import { query, queryOne } from '../db/pool.js';
import { BusinessError } from '../errors.js';
import { ResultCode } from '../utils/result.js';
import { toCamelCase, toCamelCaseList } from '../utils/helpers.js';

function buildTree(categories) {
  const vos = toCamelCaseList(categories);
  const roots = [];
  for (const vo of vos) {
    if (!vo.parentId || vo.parentId === 0) {
      vo.children = findChildren(vo.id, vos);
      roots.push(vo);
    }
  }
  return roots;
}

function findChildren(parentId, all) {
  const children = all.filter((vo) => parentId === vo.parentId);
  if (!children.length) return null;
  for (const child of children) {
    child.children = findChildren(child.id, all);
  }
  return children;
}

function flattenCategories(category) {
  const list = [category];
  if (category.children) {
    category.children.forEach((child) => list.push(...flattenCategories(child)));
  }
  return list;
}

export async function listEnabledCategories() {
  const rows = await query('SELECT * FROM category WHERE status = 1 ORDER BY sort_order ASC');
  return buildTree(rows);
}

export async function listAllCategories() {
  const rows = await query('SELECT * FROM category ORDER BY sort_order ASC');
  return buildTree(rows);
}

export async function createCategory(dto) {
  await checkCodeUnique(dto.categoryCode, null);
  const result = await query(
    'INSERT INTO category (parent_id, category_name, category_code, description, sort_order, status) VALUES (?, ?, ?, ?, ?, ?)',
    [dto.parentId ?? 0, dto.categoryName, dto.categoryCode, dto.description ?? null, dto.sortOrder ?? 0, dto.status ?? 1]
  );
  return result.insertId;
}

export async function updateCategory(id, dto) {
  await getCategoryOrThrow(id);
  await checkCodeUnique(dto.categoryCode, id);
  await query(
    'UPDATE category SET parent_id = ?, category_name = ?, category_code = ?, description = ?, sort_order = ?, status = ? WHERE id = ?',
    [dto.parentId ?? 0, dto.categoryName, dto.categoryCode, dto.description ?? null, dto.sortOrder ?? 0, dto.status ?? 1, id]
  );
}

export async function deleteCategory(id) {
  await getCategoryOrThrow(id);
  const childCount = await queryOne('SELECT COUNT(*) AS cnt FROM category WHERE parent_id = ?', [id]);
  if (childCount.cnt > 0) throw new BusinessError('该分类下存在子分类，无法删除');
  const bookCount = await queryOne('SELECT COUNT(*) AS cnt FROM book_category WHERE category_id = ?', [id]);
  if (bookCount.cnt > 0) throw new BusinessError('该分类下存在图书，无法删除');
  await query('DELETE FROM category WHERE id = ?', [id]);
}

async function checkCodeUnique(code, excludeId) {
  const sql = excludeId
    ? 'SELECT COUNT(*) AS cnt FROM category WHERE category_code = ? AND id != ?'
    : 'SELECT COUNT(*) AS cnt FROM category WHERE category_code = ?';
  const params = excludeId ? [code, excludeId] : [code];
  const row = await queryOne(sql, params);
  if (row.cnt > 0) throw new BusinessError('分类编码已存在');
}

async function getCategoryOrThrow(id) {
  const category = await queryOne('SELECT * FROM category WHERE id = ?', [id]);
  if (!category) throw new BusinessError(ResultCode.NOT_FOUND, '分类不存在');
  return category;
}

export { flattenCategories };
