import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import mysql from 'mysql2/promise';
import dotenv from 'dotenv';

dotenv.config();

const __dirname = path.dirname(fileURLToPath(import.meta.url));

async function main() {
  const conn = await mysql.createConnection({
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '3306', 10),
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '123456',
    database: process.env.DB_NAME || 'reading_website',
    multipleStatements: true,
    charset: 'utf8mb4',
  });

  const dataSql = fs.readFileSync(path.join(__dirname, '../sql/data.sql'), 'utf8');
  await conn.query(dataSql);
  console.log('Seed data imported successfully');
  await conn.end();
}

main().catch((err) => {
  console.error('Seed failed:', err.message);
  process.exit(1);
});
