import { randomUUID } from 'crypto';
import { BusinessError } from '../errors.js';
import { ResultCode } from './result.js';

const ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
const MAX_FILE_SIZE = 5 * 1024 * 1024;

export function validateImage(file) {
  if (!file || !file.size) {
    throw new BusinessError('上传文件不能为空');
  }
  if (file.size > MAX_FILE_SIZE) {
    throw new BusinessError('文件大小不能超过5MB');
  }
  const originalFilename = file.originalname;
  if (!originalFilename || !originalFilename.includes('.')) {
    throw new BusinessError(ResultCode.BAD_REQUEST.code, '文件格式不正确');
  }
  const extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
  if (!ALLOWED_EXTENSIONS.includes(extension)) {
    throw new BusinessError(ResultCode.BAD_REQUEST.code, '仅支持 jpg、jpeg、png、gif、webp 格式');
  }
}

export function generateFilename(originalFilename) {
  const extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
  return randomUUID().replace(/-/g, '') + '.' + extension;
}
