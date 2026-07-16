import { query, queryOne } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode } from '../utils/result.js';
import { toCamelCase } from '../utils/helpers.js';

export async function getProgress(userId, bookId) {
  const progress = await queryOne(
    'SELECT * FROM reading_progress WHERE user_id = ? AND book_id = ?',
    [userId, bookId]
  );
  if (!progress) return null;
  const vo = toCamelCase(progress);
  const chapter = await queryOne('SELECT chapter_no, chapter_title FROM chapter WHERE id = ?', [progress.chapter_id]);
  if (chapter) {
    vo.chapterNo = chapter.chapter_no;
    vo.chapterTitle = chapter.chapter_title;
  }
  return vo;
}

export async function saveOrUpdateProgress(userId, dto) {
  const chapter = await queryOne('SELECT * FROM chapter WHERE id = ?', [dto.chapterId]);
  if (!chapter || chapter.publish_status !== 1) {
    throw new BusinessError(ResultCode.NOT_FOUND, '章节不存在或未发布');
  }
  if (chapter.book_id !== dto.bookId) {
    throw new BusinessError('章节与图书不匹配');
  }

  const existing = await queryOne(
    'SELECT * FROM reading_progress WHERE user_id = ? AND book_id = ?',
    [userId, dto.bookId]
  );

  const chapterOffset = dto.chapterOffset ?? 0;
  const progressPercent = dto.progressPercent ?? 0;

  if (existing) {
    await query(
      `UPDATE reading_progress SET chapter_id = ?, chapter_offset = ?, progress_percent = ?, last_read_at = NOW()
       WHERE id = ?`,
      [dto.chapterId, chapterOffset, progressPercent, existing.id]
    );
  } else {
    await query(
      `INSERT INTO reading_progress (user_id, book_id, chapter_id, chapter_offset, progress_percent, last_read_at)
       VALUES (?, ?, ?, ?, ?, NOW())`,
      [userId, dto.bookId, dto.chapterId, chapterOffset, progressPercent]
    );
  }

  const bookshelf = await queryOne(
    'SELECT * FROM bookshelf WHERE user_id = ? AND book_id = ?',
    [userId, dto.bookId]
  );
  if (bookshelf) {
    let readingStatus = bookshelf.reading_status;
    if (readingStatus == null || readingStatus === 0) readingStatus = 1;
    if (progressPercent >= 100) readingStatus = 2;
    await query(
      'UPDATE bookshelf SET last_read_at = NOW(), reading_status = ? WHERE id = ?',
      [readingStatus, bookshelf.id]
    );
  }
}
