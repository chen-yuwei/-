import fs from 'fs';
import path from 'path';
import config from '../config.js';
import { BusinessError } from '../errors.js';
import { validateImage, generateFilename } from '../utils/fileUtil.js';

export function upload(file, subDir = 'common') {
  validateImage(file);
  const filename = generateFilename(file.originalname);
  const dirPath = path.join(config.file.uploadDir, subDir);
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true });
  }
  const filePath = path.join(dirPath, filename);
  fs.writeFileSync(filePath, file.buffer);
  return {
    filename,
    url: `${config.file.accessUrl}/${subDir}/${filename}`,
  };
}
