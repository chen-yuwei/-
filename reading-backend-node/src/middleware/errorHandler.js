import { BusinessError } from '../errors.js';
import { ResultCode, fail } from '../utils/result.js';

export function errorHandler(err, req, res, next) {
  if (err instanceof BusinessError) {
    return res.status(getHttpStatus(err.code)).json(fail(err.code, err.message));
  }
  console.error('系统异常', err);
  return res.status(500).json(fail(ResultCode.ERROR.code, '服务器内部错误'));
}

function getHttpStatus(code) {
  if (code === 400) return 400;
  if (code === 401) return 401;
  if (code === 403) return 403;
  if (code === 404) return 404;
  if (code === 409) return 409;
  return 500;
}
