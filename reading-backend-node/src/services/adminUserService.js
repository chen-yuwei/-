import { query, queryOne } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode, pageResult } from '../utils/result.js';
import { toCamelCase, toCamelCaseList } from '../utils/helpers.js';
import { toUserVO } from './authService.js';

export async function listUsers(queryDto) {
  const { pageNum = 1, pageSize = 10, username, nickname, role, status } = queryDto;
  const conditions = [];
  const params = [];

  if (username) { conditions.push('username LIKE ?'); params.push(`%${username}%`); }
  if (nickname) { conditions.push('nickname LIKE ?'); params.push(`%${nickname}%`); }
  if (role) { conditions.push('role = ?'); params.push(role); }
  if (status != null) { conditions.push('status = ?'); params.push(status); }

  const where = conditions.length ? `WHERE ${conditions.join(' AND ')}` : '';
  const countRow = await queryOne(`SELECT COUNT(*) AS total FROM sys_user ${where}`, params);
  const offset = (pageNum - 1) * pageSize;
  const rows = await query(
    `SELECT * FROM sys_user ${where} ORDER BY created_at DESC LIMIT ? OFFSET ?`,
    [...params, Number(pageSize), Number(offset)]
  );
  return pageResult(rows.map(toUserVO), countRow.total, pageNum, pageSize);
}

export async function getUserDetail(id) {
  const user = await queryOne('SELECT * FROM sys_user WHERE id = ?', [id]);
  if (!user) throw new BusinessError(ResultCode.NOT_FOUND, '用户不存在');
  return toUserVO(user);
}

export async function updateUserStatus(operatorId, userId, status) {
  if (operatorId === userId && status === 0) {
    throw new BusinessError('不能禁用自己的账号');
  }
  const user = await queryOne('SELECT * FROM sys_user WHERE id = ?', [userId]);
  if (!user) throw new BusinessError(ResultCode.NOT_FOUND, '用户不存在');
  await query('UPDATE sys_user SET status = ? WHERE id = ?', [status, userId]);
}
