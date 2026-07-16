import { query, queryOne } from '../db/pool.js';
import { BusinessError, ForbiddenError } from '../errors.js';
import { ResultCode, pageResult } from '../utils/result.js';
import { toCamelCase } from '../utils/helpers.js';

async function toVO(comment) {
  const vo = toCamelCase(comment);
  const user = await queryOne('SELECT nickname, avatar_url FROM sys_user WHERE id = ?', [comment.user_id]);
  if (user) {
    vo.nickname = user.nickname;
    vo.avatarUrl = user.avatar_url;
  }
  return vo;
}

async function countReplies(parentId) {
  const row = await queryOne(
    'SELECT COUNT(*) AS cnt FROM book_comment WHERE parent_id = ? AND status = 1',
    [parentId]
  );
  return row.cnt;
}

async function getReplies(parentId) {
  const replies = await query(
    'SELECT * FROM book_comment WHERE parent_id = ? AND status = 1 ORDER BY created_at ASC',
    [parentId]
  );
  const result = [];
  for (const reply of replies) {
    result.push(await toVO(reply));
  }
  return result;
}

export async function listBookComments(bookId, pageNum, pageSize) {
  const countRow = await queryOne(
    'SELECT COUNT(*) AS total FROM book_comment WHERE book_id = ? AND parent_id IS NULL AND status = 1',
    [bookId]
  );
  const offset = (pageNum - 1) * pageSize;
  const rows = await query(
    'SELECT * FROM book_comment WHERE book_id = ? AND parent_id IS NULL AND status = 1 ORDER BY created_at DESC LIMIT ? OFFSET ?',
    [bookId, Number(pageSize), Number(offset)]
  );
  const records = [];
  for (const comment of rows) {
    const vo = await toVO(comment);
    vo.replyCount = await countReplies(comment.id);
    vo.replies = await getReplies(comment.id);
    records.push(vo);
  }
  return pageResult(records, countRow.total, pageNum, pageSize);
}

export async function createComment(userId, dto) {
  const book = await queryOne('SELECT * FROM book WHERE id = ?', [dto.bookId]);
  if (!book || book.publish_status !== 1) {
    throw new BusinessError(ResultCode.NOT_FOUND, '图书不存在或已下架');
  }

  let score = dto.score;
  if (dto.parentId != null) {
    const parent = await queryOne('SELECT * FROM book_comment WHERE id = ?', [dto.parentId]);
    if (!parent || parent.book_id !== dto.bookId) {
      throw new BusinessError('回复的评论不存在');
    }
    score = null;
  } else {
    if (score == null || score < 1 || score > 5) {
      throw new BusinessError('一级评论评分范围为1至5分');
    }
  }

  await query(
    'INSERT INTO book_comment (user_id, book_id, parent_id, content, score, like_count, status) VALUES (?, ?, ?, ?, ?, 0, 1)',
    [userId, dto.bookId, dto.parentId ?? null, dto.content, score]
  );

  if (dto.parentId == null) {
    await refreshBookScore(dto.bookId);
  }
}

export async function deleteComment(userId, commentId, isAdmin) {
  const comment = await queryOne('SELECT * FROM book_comment WHERE id = ?', [commentId]);
  if (!comment) throw new BusinessError(ResultCode.NOT_FOUND, '评论不存在');
  if (!isAdmin && comment.user_id !== userId) {
    throw new ForbiddenError('只能删除自己的评论');
  }

  const bookId = comment.book_id;
  const isTopLevel = comment.parent_id == null;

  if (isTopLevel) {
    await query('DELETE FROM book_comment WHERE parent_id = ?', [commentId]);
  }
  await query('DELETE FROM book_comment WHERE id = ?', [commentId]);

  if (isTopLevel) {
    await refreshBookScore(bookId);
  }
}

export async function adminListComments(queryDto) {
  const { pageNum = 1, pageSize = 10, bookId, userId, status } = queryDto;
  const conditions = ['parent_id IS NULL'];
  const params = [];

  if (bookId) { conditions.push('book_id = ?'); params.push(bookId); }
  if (userId) { conditions.push('user_id = ?'); params.push(userId); }
  if (status != null) { conditions.push('status = ?'); params.push(status); }

  const where = conditions.join(' AND ');
  const countRow = await queryOne(`SELECT COUNT(*) AS total FROM book_comment WHERE ${where}`, params);
  const offset = (pageNum - 1) * pageSize;
  const rows = await query(
    `SELECT * FROM book_comment WHERE ${where} ORDER BY created_at DESC LIMIT ? OFFSET ?`,
    [...params, Number(pageSize), Number(offset)]
  );
  const records = [];
  for (const comment of rows) {
    records.push(await toVO(comment));
  }
  return pageResult(records, countRow.total, pageNum, pageSize);
}

export async function updateCommentStatus(id, status) {
  const comment = await queryOne('SELECT * FROM book_comment WHERE id = ?', [id]);
  if (!comment) throw new BusinessError(ResultCode.NOT_FOUND, '评论不存在');
  await query('UPDATE book_comment SET status = ? WHERE id = ?', [status, id]);
  if (comment.parent_id == null) {
    await refreshBookScore(comment.book_id);
  }
}

export async function adminDeleteComment(id) {
  return deleteComment(null, id, true);
}

export async function refreshBookScore(bookId) {
  const avgRow = await queryOne(
    'SELECT IFNULL(AVG(score), 0) AS avgScore FROM book_comment WHERE book_id = ? AND parent_id IS NULL AND status = 1 AND score IS NOT NULL',
    [bookId]
  );
  const countRow = await queryOne(
    'SELECT COUNT(*) AS cnt FROM book_comment WHERE book_id = ? AND parent_id IS NULL AND status = 1',
    [bookId]
  );
  const avgScore = Number(Number(avgRow.avgScore).toFixed(2));
  await query('UPDATE book SET average_score = ?, comment_count = ? WHERE id = ?', [avgScore, countRow.cnt, bookId]);
}
