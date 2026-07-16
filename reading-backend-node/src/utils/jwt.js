import jwt from 'jsonwebtoken';
import config from '../config.js';

export function generateToken(userId, username, role) {
  return jwt.sign({ userId, username, role }, config.jwt.secret, {
    subject: username,
    expiresIn: config.jwt.expiration / 1000,
  });
}

export function parseToken(token) {
  return jwt.verify(token, config.jwt.secret);
}

export function validateToken(token) {
  try {
    const claims = parseToken(token);
    return claims.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}
