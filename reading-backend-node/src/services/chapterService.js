import { query, queryOne, withTransaction } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode, pageResult } from '../utils/result.js';
import { toCamelCase, calcWordCount } from '../utils/helpers.js';
import * as bookService from './bookService.js';

function toChapterVO(chapter, includeContent) {
  const vo = toCamelCase(chapter);
  if (!includeContent) vo.content = null;
  return vo;
}

async function getChapterOrThrow(id) {
  const chapter = await queryOne('SELECT * FROM chapter WHERE id = ?', [id]);
  if (!chapter) throw new BusinessError(ResultCode.NOT_FOUND, '章节不存在');
  return chapter;
}

async function validateBookExists(bookId) {
  const book = await queryOne('SELECT id FROM book WHERE id = ?', [bookId]);
  if (!book) throw new BusinessError(ResultCode.NOT_FOUND, '图书不存在');
}

async function getProgressChapterId(userId, bookId) {
  const progress = await queryOne(
    'SELECT chapter_id FROM reading_progress WHERE user_id = ? AND book_id = ?',
    [userId, bookId]
  );
  return progress?.chapter_id ?? null;
}

export async function listChapters(bookId, pageNum, pageSize, userId, onlyPublished) {
  await validateBookExists(bookId);
  const conditions = ['book_id = ?'];
  const params = [bookId];
  if (onlyPublished) {
    conditions.push('publish_status = 1');
  }
  const where = conditions.join(' AND ');
  const countRow = await queryOne(`SELECT COUNT(*) AS total FROM chapter WHERE ${where}`, params);
  const offset = (pageNum - 1) * pageSize;
  const rows = await query(
    `SELECT * FROM chapter WHERE ${where} ORDER BY chapter_no ASC LIMIT ? OFFSET ?`,
    [...params, Number(pageSize), Number(offset)]
  );
  const progressChapterId = userId ? await getProgressChapterId(userId, bookId) : null;
  const records = rows.map((chapter) => {
    const vo = toChapterVO(chapter, false);
    vo.isCurrent = progressChapterId != null && progressChapterId === chapter.id;
    return vo;
  });
  return pageResult(records, countRow.total, pageNum, pageSize);
}

export async function getChapterDetail(chapterId, userId, clientKey, onlyPublished) {
  const chapter = await getChapterOrThrow(chapterId);
  if (onlyPublished && chapter.publish_status !== 1) {
    throw new BusinessError(ResultCode.NOT_FOUND, '章节不存在或未发布');
  }
  const book = await queryOne('SELECT * FROM book WHERE id = ?', [chapter.book_id]);
  if (onlyPublished && (!book || book.publish_status !== 1)) {
    throw new BusinessError(ResultCode.NOT_FOUND, '图书不存在或已下架');
  }
  if (onlyPublished) {
    await bookService.incrementViewCount(chapter.book_id, userId, clientKey);
  }
  const vo = toChapterVO(chapter, true);
  if (book) vo.bookTitle = book.title;
  return vo;
}

export async function getPreviousChapter(chapterId, onlyPublished) {
  const current = await getChapterOrThrow(chapterId);
  const conditions = ['book_id = ?', 'chapter_no < ?'];
  const params = [current.book_id, current.chapter_no];
  if (onlyPublished) {
    conditions.push('publish_status = 1');
  }
  const prev = await queryOne(
    `SELECT * FROM chapter WHERE ${conditions.join(' AND ')} ORDER BY chapter_no DESC LIMIT 1`,
    params
  );
  return prev ? toChapterVO(prev, onlyPublished) : null;
}

export async function getNextChapter(chapterId, onlyPublished) {
  const current = await getChapterOrThrow(chapterId);
  const conditions = ['book_id = ?', 'chapter_no > ?'];
  const params = [current.book_id, current.chapter_no];
  if (onlyPublished) {
    conditions.push('publish_status = 1');
  }
  const next = await queryOne(
    `SELECT * FROM chapter WHERE ${conditions.join(' AND ')} ORDER BY chapter_no ASC LIMIT 1`,
    params
  );
  return next ? toChapterVO(next, onlyPublished) : null;
}

export async function adminListChapters(bookId, pageNum, pageSize) {
  return listChapters(bookId, pageNum, pageSize, null, false);
}

export async function createChapter(dto) {
  await validateBookExists(dto.bookId);
  await checkChapterNoUnique(dto.bookId, dto.chapterNo, null);
  return withTransaction(async (conn) => {
    const wordCount = calcWordCount(dto.content);
    const publishedAt = dto.publishStatus === 1 ? new Date() : null;
    const [result] = await conn.execute(
      `INSERT INTO chapter (book_id, chapter_no, chapter_title, content, word_count, is_free, publish_status, published_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [dto.bookId, dto.chapterNo, dto.chapterTitle, dto.content, wordCount, dto.isFree, dto.publishStatus, publishedAt]
    );
    await refreshBookStats(conn, dto.bookId);
    return result.insertId;
  });
}

export async function updateChapter(id, dto) {
  const chapter = await getChapterOrThrow(id);
  await checkChapterNoUnique(dto.bookId, dto.chapterNo, id);
  return withTransaction(async (conn) => {
    const wordCount = calcWordCount(dto.content);
    let publishedAt = chapter.published_at;
    if (dto.publishStatus === 1 && !publishedAt) publishedAt = new Date();
    await conn.execute(
      `UPDATE chapter SET book_id = ?, chapter_no = ?, chapter_title = ?, content = ?, word_count = ?,
        is_free = ?, publish_status = ?, published_at = ? WHERE id = ?`,
      [dto.bookId, dto.chapterNo, dto.chapterTitle, dto.content, wordCount, dto.isFree, dto.publishStatus, publishedAt, id]
    );
    await refreshBookStats(conn, chapter.book_id);
  });
}

export async function deleteChapter(id) {
  const chapter = await getChapterOrThrow(id);
  return withTransaction(async (conn) => {
    await conn.execute('DELETE FROM chapter WHERE id = ?', [id]);
    await refreshBookStats(conn, chapter.book_id);
  });
}

async function refreshBookStats(conn, bookId) {
  const [chapters] = await conn.execute(
    'SELECT word_count FROM chapter WHERE book_id = ? AND publish_status = 1',
    [bookId]
  );
  const totalChapters = chapters.length;
  const totalWords = chapters.reduce((sum, c) => sum + (c.word_count || 0), 0);
  await conn.execute('UPDATE book SET total_chapters = ?, total_words = ? WHERE id = ?', [totalChapters, totalWords, bookId]);
}

async function checkChapterNoUnique(bookId, chapterNo, excludeId) {
  const sql = excludeId
    ? 'SELECT COUNT(*) AS cnt FROM chapter WHERE book_id = ? AND chapter_no = ? AND id != ?'
    : 'SELECT COUNT(*) AS cnt FROM chapter WHERE book_id = ? AND chapter_no = ?';
  const params = excludeId ? [bookId, chapterNo, excludeId] : [bookId, chapterNo];
  const row = await queryOne(sql, params);
  if (row.cnt > 0) throw new BusinessError('同一本书中章节序号不能重复');
}
