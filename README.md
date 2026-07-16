# 在线图书阅读网站 (Reading Website)

一个完整的在线小说/电子书阅读平台，包含用户端阅读体验和管理员后台管理功能。

## 一、项目整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        浏览器 (Vue 3 + Vite)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │   用户端页面   │  │  管理端页面   │  │  Pinia + Vue Router  │   │
│  └──────────────┘  └──────────────┘  └──────────────────────┘   │
│                          │ Axios (/api 代理)                     │
└──────────────────────────┼──────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│              Node.js + Express 后端 (端口 8080)                  │
│  ┌────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │   Routes   │→ │  Services   │→ │  mysql2 (SQL 查询)       │  │
│  └────────────┘  └─────────────┘  └─────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │ JWT 中间件 + 角色权限控制 (USER / ADMIN)                     │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ 统一响应    │  │ 全局异常处理  │  │ 文件上传 / 静态资源映射   │  │
│  └────────────┘  └─────────────┘  └─────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    MySQL 8.0 (reading_website)                   │
│   sys_user │ book │ chapter │ category │ bookshelf │ comment ... │
└─────────────────────────────────────────────────────────────────┘
```

### 技术选型

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3, Vite, JavaScript, Vue Router, Pinia, Axios, Element Plus, ECharts |
| 后端 | Node.js 18+, Express, JWT, bcryptjs, mysql2, multer |

### 认证与权限流程

1. 用户登录 → 后端校验账号密码（BCrypt）→ 签发 JWT Token
2. 前端将 Token 存入 `localStorage`，Axios 拦截器自动附加 `Authorization: Bearer <token>`
3. 后端 JWT 中间件解析 Token，挂载用户信息到请求
4. 路由中间件根据角色（USER / ADMIN）控制接口访问
5. Vue Router 路由守卫根据角色控制页面访问（`/admin/*` 仅 ADMIN 可访问）

---

## 二、后端目录结构

```
reading-backend-node/
├── package.json
├── .env.example
├── sql/
│   ├── schema.sql
│   └── data.sql
├── scripts/
│   └── seed.js                    # 数据初始化脚本
└── src/
    ├── app.js                     # 入口文件
    ├── config.js                  # 环境配置
    ├── db/
    │   └── pool.js                # MySQL 连接池
    ├── middleware/
    │   ├── auth.js                # JWT 认证 + 权限
    │   └── errorHandler.js        # 全局异常处理
    ├── routes/
    │   └── index.js               # 全部 API 路由
    ├── services/
    │   ├── authService.js
    │   ├── userService.js
    │   ├── bookService.js
    │   ├── chapterService.js
    │   ├── categoryService.js
    │   ├── bookshelfService.js
    │   ├── readingProgressService.js
    │   ├── readingHistoryService.js
    │   ├── commentService.js
    │   ├── fileService.js
    │   ├── adminUserService.js
    │   └── adminStatisticsService.js
    ├── utils/
    │   ├── result.js              # 统一响应
    │   ├── jwt.js
    │   ├── helpers.js
    │   ├── fileUtil.js
    │   └── viewCountCache.js
    └── errors.js
```

> 原 Java 版后端保留在 `reading-backend/` 目录，已不再使用。

---

## 三、前端目录结构

```
reading-frontend/
├── package.json
├── vite.config.js
├── index.html
└── src/
    ├── main.js                              # 入口文件
    ├── App.vue
    ├── api/
    │   ├── auth.js
    │   ├── user.js
    │   ├── book.js
    │   ├── chapter.js
    │   ├── category.js
    │   ├── bookshelf.js
    │   ├── readingProgress.js
    │   ├── readingHistory.js
    │   ├── comment.js
    │   ├── file.js
    │   └── admin.js
    ├── assets/
    │   └── styles/
    │       ├── global.css
    │       └── reader.css
    ├── components/
    │   ├── BookCard.vue
    │   ├── Pagination.vue
    │   ├── CommentList.vue
    │   └── SearchBar.vue
    ├── layout/
    │   ├── UserLayout.vue                   # 用户端布局
    │   └── AdminLayout.vue                  # 管理端布局
    ├── router/
    │   └── index.js                         # 路由 + 守卫
    ├── stores/
    │   └── user.js                          # Pinia 用户状态
    ├── utils/
    │   ├── request.js                       # Axios 封装
    │   └── auth.js                          # Token 工具
    └── views/
        ├── user/
        │   ├── Home.vue                     # 首页
        │   ├── Login.vue
        │   ├── Register.vue
        │   ├── BookList.vue
        │   ├── BookDetail.vue
        │   ├── Reader.vue                   # 在线阅读
        │   ├── CategoryBooks.vue
        │   ├── Bookshelf.vue
        │   ├── History.vue
        │   └── Profile.vue
        ├── admin/
        │   ├── Dashboard.vue                # 后台首页
        │   ├── UserManage.vue
        │   ├── BookManage.vue
        │   ├── BookForm.vue
        │   ├── ChapterManage.vue
        │   ├── CategoryManage.vue
        │   └── CommentManage.vue
        └── NotFound.vue
```

---

## 四、数据库表关系说明

```
sys_user (用户)
    │
    ├──1:N──► bookshelf (书架) ◄──N:1── book (图书)
    │
    ├──1:N──► reading_progress (阅读进度) ──N:1── book
    │                              └──N:1── chapter (章节)
    │
    ├──1:N──► reading_history (阅读历史) ──N:1── book
    │                              └──N:1── chapter
    │
    └──1:N──► book_comment (评论) ──N:1── book
                    │
                    └──1:N──► book_comment (回复，自关联 parent_id)

book (图书)
    ├──1:N──► chapter (章节)
    └──N:M──► category (分类)  [通过 book_category 关联表]

category (分类)
    └── 自关联 parent_id 支持二级分类
```

### 核心约束

| 约束 | 说明 |
|------|------|
| `sys_user.username` UNIQUE | 用户名唯一 |
| `sys_user.email` UNIQUE | 邮箱唯一 |
| `bookshelf(user_id, book_id)` UNIQUE | 同一用户不能重复收藏 |
| `reading_progress(user_id, book_id)` UNIQUE | 每用户每本书仅一条进度 |
| `chapter(book_id, chapter_no)` UNIQUE | 同一本书章节序号不重复 |
| 外键级联 | 删除图书时级联处理关联数据 |

---

## 五、项目启动步骤

### 前置条件

- Node.js 18+
- MySQL 8.0+

### 1. 初始化数据库

```bash
# Windows（推荐）
cmd /c "mysql -u root -p < reading-backend-node\sql\schema.sql"

# Linux / macOS
mysql -u root -p < reading-backend-node/sql/schema.sql
```

然后导入初始数据：

```bash
cd reading-backend-node
npm install
npm run seed
```

### 2. 修改配置（如需要）

复制 `reading-backend-node/.env.example` 为 `.env`，修改数据库密码等配置。

### 3. 启动后端

```bash
cd reading-backend-node
npm start
```

后端运行在 http://localhost:8080

### 4. 启动前端

```bash
cd reading-frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173

### 5. 访问系统

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | user123 |
| 普通用户 | reader01 | user123 |

- 用户端：http://localhost:5173
- 管理后台：http://localhost:5173/admin/dashboard

---

## 第五部分：运行说明

### 1. 如何创建数据库

确保 MySQL 8.0 已启动，在命令行执行：

```bash
# Windows（推荐用 cmd 重定向）
cmd /c "mysql -u root -p < reading-backend-node\sql\schema.sql"

# Linux / macOS
mysql -u root -p < reading-backend-node/sql/schema.sql
```

然后导入初始数据：

```bash
cd reading-backend-node
npm run seed
```

执行完成后会创建 `reading_website` 数据库及全部表结构和初始数据。

### 2. 如何修改数据库密码

编辑 `reading-backend-node/.env`：

```env
DB_USER=root
DB_PASSWORD=你的密码    # 默认 123456
```

### 3. 如何启动后端

**环境要求**：Node.js 18+

```bash
cd reading-backend-node
npm install
npm start
```

启动成功后访问：http://localhost:8080

### 4. 如何启动前端

**环境要求**：Node.js 18+

```bash
cd reading-frontend
npm install
npm run dev
```

启动成功后访问：http://localhost:5173

前端已通过 Vite 代理将 `/api` 和 `/uploads` 转发到后端 `http://localhost:8080`。

### 5. 如何登录管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |

登录后访问：http://localhost:5173/admin/dashboard

### 6. 如何登录普通用户账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| user | user123 | USER |
| reader01 | user123 | USER |

### 7. 常见启动错误及解决方法

| 错误 | 原因 | 解决方法 |
|------|------|----------|
| `Communications link failure` | MySQL 未启动或连接信息错误 | 启动 MySQL 服务，检查 `.env` 中的 DB_HOST/DB_PASSWORD |
| `Access denied for user` | 数据库用户名或密码错误 | 修改 `.env` 中的 `DB_USER` 和 `DB_PASSWORD` |
| `Unknown database 'reading_website'` | 未执行建库脚本 | 运行 `sql/schema.sql` |
| `Table doesn't exist` | 只导入了数据未建表 | 先运行 `schema.sql`，再运行 `npm run seed` |
| 前端 401 / 接口报错 | 未登录或 Token 过期 | 重新登录；检查 Axios 是否携带 Authorization 头 |
| 前端代理失败 | 后端未启动 | 先启动后端，再启动前端 |
| 文件上传失败 | uploads 目录无写权限 | 确保 `reading-backend-node/uploads` 文件夹可写 |
| `Port 8080 already in use` | 端口被占用 | 修改 `.env` 中的 `PORT` |

---

## 项目结构总览

```
Reading/
├── README.md
├── reading-backend-node/     # Node.js 后端（当前使用）
│   ├── package.json
│   ├── .env
│   ├── sql/
│   └── src/
├── reading-backend/          # Java 后端（已弃用，保留作参考）
│   ├── pom.xml
│   └── src/main/
├── reading-frontend/         # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
```

---

## 默认账号

- **管理员**：admin / admin123
- **普通用户**：user / user123
