import dotenv from 'dotenv';

dotenv.config();

export default {
  port: parseInt(process.env.PORT || '8080', 10),
  db: {
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '3306', 10),
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '123456',
    database: process.env.DB_NAME || 'reading_website',
  },
  jwt: {
    secret: process.env.JWT_SECRET || 'ReadingWebsiteJwtSecretKey2024MustBeLongEnoughForHS256Algorithm',
    expiration: parseInt(process.env.JWT_EXPIRATION || '86400000', 10),
  },
  file: {
    uploadDir: process.env.UPLOAD_DIR || './uploads',
    accessUrl: process.env.ACCESS_URL || '/uploads',
  },
};
