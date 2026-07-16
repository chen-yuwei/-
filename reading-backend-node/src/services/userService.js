import bcrypt from 'bcryptjs';
import { query, queryOne } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode } from '../utils/result.js';
import { toUserVO } from './authService.js';

async function getUserOrThrow(userId) {
  const user = await queryOne('SELECT * FROM sys_user WHERE id = ?', [userId]);
  if (!user) throw new BusinessError(ResultCode.NOT_FOUND, '用户不存在');
  return user;
}

export async function getProfile(userId) {
  return toUserVO(await getUserOrThrow(userId));
}

export async function updateProfile(userId, dto) {
  const user = await getUserOrThrow(userId);

  if (dto.email && dto.email !== user.email) {
    const count = await queryOne('SELECT COUNT(*) AS cnt FROM sys_user WHERE email = ? AND id != ?', [dto.email, userId]);
    if (count.cnt > 0) throw new BusinessError('邮箱已被使用');
    user.email = dto.email;
  }
  if (dto.phone && dto.phone !== user.phone) {
    const count = await queryOne('SELECT COUNT(*) AS cnt FROM sys_user WHERE phone = ? AND id != ?', [dto.phone, userId]);
    if (count.cnt > 0) throw new BusinessError('手机号已被使用');
    user.phone = dto.phone;
  }
  if (dto.nickname) user.nickname = dto.nickname;
  if (dto.avatarUrl) user.avatar_url = dto.avatarUrl;

  await query(
    'UPDATE sys_user SET nickname = ?, avatar_url = ?, email = ?, phone = ? WHERE id = ?',
    [user.nickname, user.avatar_url, user.email, user.phone, userId]
  );
  return toUserVO(await getUserOrThrow(userId));
}

export async function updatePassword(userId, dto) {
  if (dto.newPassword !== dto.confirmPassword) {
    throw new BusinessError('两次输入的新密码不一致');
  }
  const user = await getUserOrThrow(userId);
  const match = await bcrypt.compare(dto.oldPassword, user.password);
  if (!match) throw new BusinessError('原密码不正确');
  const hashed = await bcrypt.hash(dto.newPassword, 10);
  await query('UPDATE sys_user SET password = ? WHERE id = ?', [hashed, userId]);
}
