import { ResultCode } from './utils/result.js';

export class BusinessError extends Error {
  constructor(codeOrMessage, message) {
    if (typeof codeOrMessage === 'number') {
      super(message);
      this.code = codeOrMessage;
    } else if (typeof codeOrMessage === 'object' && codeOrMessage.code) {
      super(message || codeOrMessage.message);
      this.code = codeOrMessage.code;
    } else {
      super(codeOrMessage);
      this.code = 400;
    }
    this.name = 'BusinessError';
  }
}

export class UnauthorizedError extends BusinessError {
  constructor(message) {
    super(ResultCode.UNAUTHORIZED, message);
  }
}

export class ForbiddenError extends BusinessError {
  constructor(message) {
    super(ResultCode.FORBIDDEN, message);
  }
}
