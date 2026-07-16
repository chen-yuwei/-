import { query, queryOne } from '../db/pool.js';

export async function getStatistics() {
  const userCount = (await queryOne('SELECT COUNT(*) AS cnt FROM sys_user')).cnt;
  const bookCount = (await queryOne('SELECT COUNT(*) AS cnt FROM book')).cnt;
  const chapterCount = (await queryOne('SELECT COUNT(*) AS cnt FROM chapter')).cnt;
  const commentCount = (await queryOne('SELECT COUNT(*) AS cnt FROM book_comment WHERE parent_id IS NULL')).cnt;

  const books = await query('SELECT view_count, favorite_count FROM book');
  const totalViewCount = books.reduce((sum, b) => sum + (b.view_count || 0), 0);
  const totalFavoriteCount = books.reduce((sum, b) => sum + (b.favorite_count || 0), 0);

  const recentUserStats = await fillRecentDays(
    await query(
      `SELECT DATE(created_at) AS statDate, COUNT(*) AS count FROM sys_user
       WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(created_at) ORDER BY statDate`
    )
  );

  const recentViewStats = await fillRecentDays(
    await query(
      `SELECT DATE(read_at) AS statDate, COUNT(*) AS count FROM reading_history
       WHERE read_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(read_at) ORDER BY statDate`
    )
  );

  const categories = await query('SELECT id, category_name FROM category WHERE status = 1 ORDER BY sort_order ASC');
  const categoryStats = [];
  for (const category of categories) {
    const count = (await queryOne('SELECT COUNT(*) AS cnt FROM book_category WHERE category_id = ?', [category.id])).cnt;
    categoryStats.push({ categoryName: category.category_name, count });
  }

  return {
    userCount,
    bookCount,
    chapterCount,
    commentCount,
    totalViewCount,
    totalFavoriteCount,
    recentUserStats,
    recentViewStats,
    categoryStats,
  };
}

async function fillRecentDays(stats) {
  const statMap = {};
  for (const m of stats) {
    const dateStr = formatDateKey(m.statDate);
    statMap[dateStr] = Number(m.count);
  }

  const result = [];
  const today = new Date();
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const dateStr = formatDateKey(date);
    result.push({ date: dateStr, count: statMap[dateStr] || 0 });
  }
  return result;
}

function formatDateKey(date) {
  if (typeof date === 'string') return date.substring(0, 10);
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
