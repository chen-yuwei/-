# Reading Backend (Node.js)

在线图书阅读网站 Node.js 后端，与 Vue 前端 API 完全兼容。

## 技术栈

- Express 4
- MySQL 2 (mysql2)
- JWT 认证
- bcryptjs 密码加密
- multer 文件上传

## 快速启动

### 1. 安装依赖

```bash
cd reading-backend-node
npm install
```

### 2. 配置环境变量

复制 `.env.example` 为 `.env`，按需修改数据库连接信息：

```env
PORT=8080
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=123456
DB_NAME=reading_website
```

### 3. 初始化数据库

```bash
# Windows（推荐用 cmd 重定向，避免编码问题）
cmd /c "mysql -u root -p < sql\schema.sql"
npm run seed

# Linux / macOS
mysql -u root -p < sql/schema.sql
npm run seed
```

### 4. 启动服务

```bash
npm start
# 或开发模式（文件变更自动重启）
npm run dev
```

服务运行在 http://localhost:8080

## 目录结构

```
reading-backend-node/
├── package.json
├── .env
├── sql/
│   ├── schema.sql
│   └── data.sql
├── scripts/
│   └── seed.js
└── src/
    ├── app.js
    ├── config.js
    ├── db/pool.js
    ├── middleware/
    ├── routes/index.js
    ├── services/
    └── utils/
```

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |
| reader01 | user123 | USER |
