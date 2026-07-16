export const ResultCode = {
  SUCCESS: { code: 200, message: 'success' },
  BAD_REQUEST: { code: 400, message: '请求参数错误' },
  UNAUTHORIZED: { code: 401, message: '未登录或登录已过期' },
  FORBIDDEN: { code: 403, message: '权限不足' },
  NOT_FOUND: { code: 404, message: '资源不存在' },
  CONFLICT: { code: 409, message: '数据冲突' },
  ERROR: { code: 500, message: '服务器内部错误' },
};

export function success(data = null) {
  return { code: 200, message: 'success', data };
}

export function fail(code, message) {
  return { code, message, data: null };
}

export function pageResult(records, total, pageNum, pageSize) {
  return {
    records,
    total: Number(total),
    pageNum: Number(pageNum),
    pageSize: Number(pageSize),
    pages: pageSize === 0 ? 0 : Math.ceil(total / pageSize),
  };
}
