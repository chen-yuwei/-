import bcrypt from 'bcryptjs';
import { query, queryOne } from '../db/pool.js';
import { BusinessError, UnauthorizedError } from '../errors.js';
import { ResultCode } from '../utils/result.js';
import { toCamelCase } from '../utils/helpers.js';
import { generateToken } from '../utils/jwt.js';

function toUserVO(user) {
  const vo = toCamelCase(user);
  delete vo.password;
  delete vo.lastLoginTime;
  delete vo.createdAt;
  delete vo.updatedAt;
  return vo;
}

export async function register(dto) {
  if (dto.password !== dto.confirmPassword) {
    throw new BusinessError('两次输入的密码不一致');
  }
  const usernameCount = await queryOne('SELECT COUNT(*) AS cnt FROM sys_user WHERE username = ?', [dto.username]);
  if (usernameCount.cnt > 0) throw new BusinessError('用户名已存在');

  const emailCount = await queryOne('SELECT COUNT(*) AS cnt FROM sys_user WHERE email = ?', [dto.email]);
  if (emailCount.cnt > 0) throw new BusinessError('邮箱已被注册');

  const hashed = await bcrypt.hash(dto.password, 10);
  await query(
    'INSERT INTO sys_user (username, password, nickname, email, role, status) VALUES (?, ?, ?, ?, ?, ?)',
    [dto.username, hashed, dto.nickname, dto.email, 'USER', 1]
  );
}

export async function login(dto) {
  const user = await queryOne('SELECT * FROM sys_user WHERE username = ?', [dto.username]);
  if (!user) {
    throw new BusinessError(ResultCode.UNAUTHORIZED, '用户名或密码错误');
  }
  const match = await bcrypt.compare(dto.password, user.password);
  if (!match) {
    throw new BusinessError(ResultCode.UNAUTHORIZED, '用户名或密码错误');
  }
  if (user.status !== 1) {
    throw new BusinessError(ResultCode.FORBIDDEN, '账号已被禁用，无法登录');
  }
  await query('UPDATE sys_user SET last_login_time = NOW() WHERE id = ?', [user.id]);
  const token = generateToken(user.id, user.username, user.role);
  return { token, user: toUserVO(user) };
}

export async function getCurrentUser(user) {
  if (!user) throw new UnauthorizedError();
  return toUserVO(user);
}

export { toUserVO };
