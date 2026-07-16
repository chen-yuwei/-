import { parseToken, validateToken } from '../utils/jwt.js';
import { queryOne } from '../db/pool.js';
import { toCamelCase } from '../utils/helpers.js';
import { UnauthorizedError, ForbiddenError } from '../errors.js';

export async function optionalAuth(req, res, next) {
  try {
    const token = resolveToken(req);
    if (token && validateToken(token)) {
      const claims = parseToken(token);
      const user = await queryOne('SELECT * FROM sys_user WHERE username = ?', [claims.username]);
      if (user) {
        req.user = toCamelCase(user);
      }
    }
    next();
  } catch (err) {
    next(err);
  }
}

export async function requireAuth(req, res, next) {
  try {
    const token = resolveToken(req);
    if (!token || !validateToken(token)) {
      throw new UnauthorizedError();
    }
    const claims = parseToken(token);
    const user = await queryOne('SELECT * FROM sys_user WHERE username = ?', [claims.username]);
    if (!user) {
      throw new UnauthorizedError();
    }
    req.user = toCamelCase(user);
    next();
  } catch (err) {
    next(err);
  }
}

export function requireAdmin(req, res, next) {
  if (!req.user || req.user.role !== 'ADMIN') {
    return next(new ForbiddenError());
  }
  next();
}

function resolveToken(req) {
  const bearerToken = req.headers.authorization;
  if (bearerToken && bearerToken.startsWith('Bearer ')) {
    return bearerToken.substring(7);
  }
  return null;
}
