export function toCamelCase(row) {
  if (!row) return row;
  const result = {};
  for (const [key, value] of Object.entries(row)) {
    const camelKey = key.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
    result[camelKey] = formatValue(value);
  }
  return result;
}

export function toCamelCaseList(rows) {
  return rows.map(toCamelCase);
}

function formatValue(value) {
  if (value instanceof Date) {
    const pad = (n) => String(n).padStart(2, '0');
    return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`;
  }
  if (typeof value === 'bigint') return Number(value);
  return value;
}

export function calcWordCount(content) {
  if (!content) return 0;
  return content.replace(/\s+/g, '').length;
}
