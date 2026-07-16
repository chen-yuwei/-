import { query, queryOne, withTransaction } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode, pageResult } from '../utils/result.js';
import { toCamelCase } from '../utils/helpers.js';
import { shouldCount } from '../utils/viewCountCache.js';
import * as categoryService from './categoryService.js';
import * as bookshelfService from './bookshelfService.js';
import * as readingProgressService from './readingProgressService.js';

function toBookVO(book) {
  return toCamelCase(book);
}

async function getBookOrThrow(id) {
  const book = await queryOne('SELECT * FROM book WHERE id = ?', [id]);
  if (!book) throw new BusinessError(ResultCode.NOT_FOUND, '图书不存在');
  return book;
}

async function getBookCategoryIds(bookId) {
  const rows = await query('SELECT category_id FROM book_category WHERE book_id = ?', [bookId]);
  return rows.map((r) => r.category_id);
}

async function getBookCategories(bookId) {
  const categoryIds = await getBookCategoryIds(bookId);
  if (!categoryIds.length) return [];
  const all = await categoryService.listEnabledCategories();
  return all.flatMap((c) => categoryService.flattenCategories(c)).filter((c) => categoryIds.includes(c.id));
}

async function getLatestChapter(bookId) {
  const chapter = await queryOne(
    'SELECT * FROM chapter WHERE book_id = ? AND publish_status = 1 ORDER BY chapter_no DESC LIMIT 1',
    [bookId]
  );
  return chapter ? toCamelCase(chapter) : null;
}

async function fillBookExtraInfo(vo, userId) {
  vo.categories = await getBookCategories(vo.id);
  vo.latestChapter = await getLatestChapter(vo.id);
  if (userId) {
    vo.inBookshelf = await bookshelfService.isInBookshelf(userId, vo.id);
    vo.readingProgress = await readingProgressService.getProgress(userId, vo.id);
  } else {
    vo.inBookshelf = false;
  }
}

function applySort(sortField, sortOrder) {
  const fieldMap = {
    viewCount: 'view_count',
    favoriteCount: 'favorite_count',
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  };
  const col = fieldMap[sortField] || 'updated_at';
  const dir = sortOrder?.toLowerCase() === 'asc' ? 'ASC' : 'DESC';
  return `ORDER BY ${col} ${dir}`;
}

async function queryBooks(queryDto, onlyPublished) {
  const {
    pageNum = 1, pageSize = 10, categoryId, title, author, keyword,
    sortField = 'updatedAt', sortOrder = 'desc',
  } = queryDto;

  const conditions = [];
  const params = [];

  if (onlyPublished) {
    conditions.push('publish_status = 1');
  }
  if (categoryId) {
    const bookIds = (await query('SELECT book_id FROM book_category WHERE category_id = ?', [categoryId])).map((r) => r.book_id);
    if (!bookIds.length) return pageResult([], 0, pageNum, pageSize);
    conditions.push(`id IN (${bookIds.map(() => '?').join(',')})`);
    params.push(...bookIds);
  }
  if (title) { conditions.push('title LIKE ?'); params.push(`%${title}%`); }
  if (author) { conditions.push('author LIKE ?'); params.push(`%${author}%`); }
  if (keyword) {
    conditions.push('(title LIKE ? OR author LIKE ?)');
    params.push(`%${keyword}%`, `%${keyword}%`);
  }

  const where = conditions.length ? `WHERE ${conditions.join(' AND ')}` : '';
  const countRow = await queryOne(`SELECT COUNT(*) AS total FROM book ${where}`, params);
  const offset = (pageNum - 1) * pageSize;
  const orderBy = applySort(sortField, sortOrder);
  const rows = await query(
    `SELECT * FROM book ${where} ${orderBy} LIMIT ? OFFSET ?`,
    [...params, Number(pageSize), Number(offset)]
  );
  return pageResult(rows.map(toBookVO), countRow.total, pageNum, pageSize);
}

export async function listBooks(queryDto, onlyPublished) {
  return queryBooks(queryDto, onlyPublished);
}

export async function getBookDetail(id, userId) {
  const book = await getBookOrThrow(id);
  if (book.publish_status !== 1) {
    throw new BusinessError(ResultCode.NOT_FOUND, '图书不存在或已下架');
  }
  const vo = toBookVO(book);
  await fillBookExtraInfo(vo, userId);
  return vo;
}

export async function getRecommendedBooks(limit) {
  const rows = await query(
    'SELECT * FROM book WHERE publish_status = 1 AND is_recommended = 1 ORDER BY updated_at DESC LIMIT ?',
    [Number(limit)]
  );
  return rows.map(toBookVO);
}

export async function getHotBooks(limit) {
  const rows = await query(
    'SELECT * FROM book WHERE publish_status = 1 ORDER BY view_count DESC LIMIT ?',
    [Number(limit)]
  );
  return rows.map(toBookVO);
}

export async function getLatestBooks(limit) {
  const rows = await query(
    'SELECT * FROM book WHERE publish_status = 1 ORDER BY updated_at DESC LIMIT ?',
    [Number(limit)]
  );
  return rows.map(toBookVO);
}

export async function searchBooks(queryDto) {
  return queryBooks(queryDto, true);
}

export async function getBooksByCategory(categoryId, queryDto) {
  return queryBooks({ ...queryDto, categoryId }, true);
}

export async function adminListBooks(queryDto) {
  return queryBooks(queryDto, false);
}

export async function adminGetBookDetail(id) {
  const book = await getBookOrThrow(id);
  const vo = toBookVO(book);
  const categoryIds = await getBookCategoryIds(id);
  const all = await categoryService.listAllCategories();
  vo.categories = all.flatMap((c) => categoryService.flattenCategories(c)).filter((c) => categoryIds.includes(c.id));
  return vo;
}

export async function createBook(dto) {
  return withTransaction(async (conn) => {
    const [result] = await conn.execute(
      `INSERT INTO book (title, author, cover_url, summary, isbn, publisher, serialize_status, publish_status, is_recommended,
        total_chapters, total_words, view_count, favorite_count, comment_count, average_score)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, 0, 0)`,
      [dto.title, dto.author, dto.coverUrl ?? null, dto.summary ?? null, dto.isbn ?? null, dto.publisher ?? null,
        dto.serializeStatus, dto.publishStatus, dto.isRecommended ?? 0]
    );
    const bookId = result.insertId;
    await saveBookCategories(conn, bookId, dto.categoryIds);
    return bookId;
  });
}

export async function updateBook(id, dto) {
  await getBookOrThrow(id);
  return withTransaction(async (conn) => {
    await conn.execute(
      `UPDATE book SET title = ?, author = ?, cover_url = ?, summary = ?, isbn = ?, publisher = ?,
        serialize_status = ?, publish_status = ?, is_recommended = ? WHERE id = ?`,
      [dto.title, dto.author, dto.coverUrl ?? null, dto.summary ?? null, dto.isbn ?? null, dto.publisher ?? null,
        dto.serializeStatus, dto.publishStatus, dto.isRecommended ?? 0, id]
    );
    await conn.execute('DELETE FROM book_category WHERE book_id = ?', [id]);
    await saveBookCategories(conn, id, dto.categoryIds);
  });
}

export async function deleteBook(id) {
  const book = await getBookOrThrow(id);
  await query('UPDATE book SET publish_status = 0 WHERE id = ?', [id]);
}

export async function updatePublishStatus(id, publishStatus) {
  await getBookOrThrow(id);
  await query('UPDATE book SET publish_status = ? WHERE id = ?', [publishStatus, id]);
}

export async function updateRecommended(id, isRecommended) {
  await getBookOrThrow(id);
  await query('UPDATE book SET is_recommended = ? WHERE id = ?', [isRecommended, id]);
}

export async function incrementViewCount(bookId, userId, clientKey) {
  const key = `${bookId}:${userId ?? clientKey}`;
  if (!shouldCount(key)) return;
  const book = await getBookOrThrow(bookId);
  await query('UPDATE book SET view_count = view_count + 1 WHERE id = ?', [bookId]);
}

async function saveBookCategories(conn, bookId, categoryIds) {
  if (!categoryIds?.length) throw new BusinessError('图书至少需要一个分类');
  for (const categoryId of categoryIds) {
    await conn.execute('INSERT INTO book_category (book_id, category_id) VALUES (?, ?)', [bookId, categoryId]);
  }
}
