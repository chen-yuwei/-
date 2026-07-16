import { query, queryOne, withTransaction } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode, pageResult } from '../utils/result.js';
import { toCamelCase } from '../utils/helpers.js';

export async function listBookshelf(userId, pageNum, pageSize) {
  const countRow = await queryOne('SELECT COUNT(*) AS total FROM bookshelf WHERE user_id = ?', [userId]);
  const offset = (pageNum - 1) * pageSize;
  const items = await query(
    'SELECT * FROM bookshelf WHERE user_id = ? ORDER BY last_read_at DESC LIMIT ? OFFSET ?',
    [userId, Number(pageSize), Number(offset)]
  );

  const records = [];
  for (const item of items) {
    const book = await queryOne('SELECT title, author, cover_url FROM book WHERE id = ?', [item.book_id]);
    const progress = await queryOne(
      'SELECT * FROM reading_progress WHERE user_id = ? AND book_id = ?',
      [userId, item.book_id]
    );
    let chapter = null;
    if (progress) {
      chapter = await queryOne('SELECT chapter_title FROM chapter WHERE id = ?', [progress.chapter_id]);
    }
    records.push(toCamelCase({
      id: item.id,
      bookId: item.book_id,
      readingStatus: item.reading_status,
      lastReadAt: item.last_read_at,
      title: book?.title,
      author: book?.author,
      coverUrl: book?.cover_url,
      progressPercent: progress?.progress_percent,
      currentChapterId: progress?.chapter_id,
      currentChapterTitle: chapter?.chapter_title,
    }));
  }
  return pageResult(records, countRow.total, pageNum, pageSize);
}

export async function addToBookshelf(userId, bookId) {
  const book = await queryOne('SELECT * FROM book WHERE id = ?', [bookId]);
  if (!book || book.publish_status !== 1) {
    throw new BusinessError(ResultCode.NOT_FOUND, '图书不存在或已下架');
  }
  const count = await queryOne(
    'SELECT COUNT(*) AS cnt FROM bookshelf WHERE user_id = ? AND book_id = ?',
    [userId, bookId]
  );
  if (count.cnt > 0) throw new BusinessError('该图书已在书架中');

  return withTransaction(async (conn) => {
    await conn.execute(
      'INSERT INTO bookshelf (user_id, book_id, reading_status) VALUES (?, ?, 0)',
      [userId, bookId]
    );
    await conn.execute('UPDATE book SET favorite_count = favorite_count + 1 WHERE id = ?', [bookId]);
  });
}

export async function removeFromBookshelf(userId, bookId) {
  const bookshelf = await queryOne(
    'SELECT * FROM bookshelf WHERE user_id = ? AND book_id = ?',
    [userId, bookId]
  );
  if (!bookshelf) throw new BusinessError(ResultCode.NOT_FOUND, '书架中不存在该图书');

  return withTransaction(async (conn) => {
    await conn.execute('DELETE FROM bookshelf WHERE id = ?', [bookshelf.id]);
    const book = await queryOne('SELECT favorite_count FROM book WHERE id = ?', [bookId]);
    if (book && book.favorite_count > 0) {
      await conn.execute('UPDATE book SET favorite_count = favorite_count - 1 WHERE id = ?', [bookId]);
    }
  });
}

export async function updateReadingStatus(userId, bookId, readingStatus) {
  const bookshelf = await queryOne(
    'SELECT * FROM bookshelf WHERE user_id = ? AND book_id = ?',
    [userId, bookId]
  );
  if (!bookshelf) throw new BusinessError(ResultCode.NOT_FOUND, '书架中不存在该图书');
  await query('UPDATE bookshelf SET reading_status = ? WHERE id = ?', [readingStatus, bookshelf.id]);
}

export async function isInBookshelf(userId, bookId) {
  if (!userId) return false;
  const count = await queryOne(
    'SELECT COUNT(*) AS cnt FROM bookshelf WHERE user_id = ? AND book_id = ?',
    [userId, bookId]
  );
  return count.cnt > 0;
}
