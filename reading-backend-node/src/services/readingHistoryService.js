import { query, queryOne } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode, pageResult } from '../utils/result.js';
import { toCamelCase } from '../utils/helpers.js';

async function toVO(history) {
  const vo = {
    id: history.id,
    bookId: history.book_id,
    chapterId: history.chapter_id,
    durationSeconds: history.duration_seconds,
    readAt: history.read_at,
  };
  const book = await queryOne('SELECT title, cover_url FROM book WHERE id = ?', [history.book_id]);
  if (book) {
    vo.bookTitle = book.title;
    vo.coverUrl = book.cover_url;
  }
  const chapter = await queryOne('SELECT chapter_title, chapter_no FROM chapter WHERE id = ?', [history.chapter_id]);
  if (chapter) {
    vo.chapterTitle = chapter.chapter_title;
    vo.chapterNo = chapter.chapter_no;
  }
  return toCamelCase(vo);
}

export async function listHistory(userId, pageNum, pageSize) {
  const countRow = await queryOne('SELECT COUNT(*) AS total FROM reading_history WHERE user_id = ?', [userId]);
  const offset = (pageNum - 1) * pageSize;
  const rows = await query(
    'SELECT * FROM reading_history WHERE user_id = ? ORDER BY read_at DESC LIMIT ? OFFSET ?',
    [userId, Number(pageSize), Number(offset)]
  );
  const records = [];
  for (const row of rows) {
    records.push(await toVO(row));
  }
  return pageResult(records, countRow.total, pageNum, pageSize);
}

export async function addHistory(userId, dto) {
  const chapter = await queryOne('SELECT id FROM chapter WHERE id = ?', [dto.chapterId]);
  if (!chapter) throw new BusinessError(ResultCode.NOT_FOUND, '章节不存在');
  await query(
    'INSERT INTO reading_history (user_id, book_id, chapter_id, duration_seconds) VALUES (?, ?, ?, ?)',
    [userId, dto.bookId, dto.chapterId, dto.durationSeconds ?? 0]
  );
}

export async function deleteHistory(userId, id) {
  const history = await queryOne('SELECT * FROM reading_history WHERE id = ?', [id]);
  if (!history || history.user_id !== userId) {
    throw new BusinessError(ResultCode.NOT_FOUND, '阅读历史不存在');
  }
  await query('DELETE FROM reading_history WHERE id = ?', [id]);
}

export async function clearHistory(userId) {
  await query('DELETE FROM reading_history WHERE user_id = ?', [userId]);
}
