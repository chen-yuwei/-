-- ============================================================
-- 在线图书阅读网站 - 数据库结构
-- 数据库：reading_website
-- 字符集：utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `reading_website`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `reading_website`;

-- -----------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `book_comment`;
DROP TABLE IF EXISTS `reading_history`;
DROP TABLE IF EXISTS `reading_progress`;
DROP TABLE IF EXISTS `bookshelf`;
DROP TABLE IF EXISTS `chapter`;
DROP TABLE IF EXISTS `book_category`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `username`        VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`        VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `nickname`        VARCHAR(50)  NOT NULL COMMENT '昵称',
    `avatar_url`      VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
    `email`           VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `role`            VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 分类表（支持一级、二级分类）
-- -----------------------------------------------------------
CREATE TABLE `category` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类主键',
    `parent_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(50)  NOT NULL COMMENT '分类编码',
    `description`   VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- -----------------------------------------------------------
-- 3. 图书表
-- -----------------------------------------------------------
CREATE TABLE `book` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '图书主键',
    `title`             VARCHAR(200)  NOT NULL COMMENT '图书名称',
    `author`            VARCHAR(100)  NOT NULL COMMENT '作者',
    `cover_url`         VARCHAR(500)  DEFAULT NULL COMMENT '封面地址',
    `summary`           TEXT          COMMENT '简介',
    `isbn`              VARCHAR(30)   DEFAULT NULL COMMENT 'ISBN',
    `publisher`         VARCHAR(100)  DEFAULT NULL COMMENT '出版社',
    `total_chapters`    INT           NOT NULL DEFAULT 0 COMMENT '章节总数',
    `total_words`       BIGINT        NOT NULL DEFAULT 0 COMMENT '总字数',
    `serialize_status`  TINYINT       NOT NULL DEFAULT 0 COMMENT '连载状态：0连载中 1已完结',
    `publish_status`    TINYINT       NOT NULL DEFAULT 0 COMMENT '发布状态：0下架 1上架',
    `is_recommended`    TINYINT       NOT NULL DEFAULT 0 COMMENT '是否推荐：0否 1是',
    `view_count`        BIGINT        NOT NULL DEFAULT 0 COMMENT '阅读量',
    `favorite_count`    INT           NOT NULL DEFAULT 0 COMMENT '收藏量',
    `comment_count`     INT           NOT NULL DEFAULT 0 COMMENT '评论数',
    `average_score`     DECIMAL(3,2)  NOT NULL DEFAULT 0.00 COMMENT '平均评分',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_title` (`title`),
    KEY `idx_author` (`author`),
    KEY `idx_publish_status` (`publish_status`),
    KEY `idx_is_recommended` (`is_recommended`),
    KEY `idx_view_count` (`view_count`),
    KEY `idx_favorite_count` (`favorite_count`),
    KEY `idx_updated_at` (`updated_at`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- -----------------------------------------------------------
-- 4. 图书分类关联表（多对多）
-- -----------------------------------------------------------
CREATE TABLE `book_category` (
    `book_id`     BIGINT   NOT NULL COMMENT '图书ID',
    `category_id` BIGINT   NOT NULL COMMENT '分类ID',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`book_id`, `category_id`),
    KEY `idx_category_id` (`category_id`),
    CONSTRAINT `fk_bc_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bc_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类关联表';

-- -----------------------------------------------------------
-- 5. 章节表
-- -----------------------------------------------------------
CREATE TABLE `chapter` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '章节主键',
    `book_id`        BIGINT        NOT NULL COMMENT '图书ID',
    `chapter_no`     INT           NOT NULL COMMENT '章节序号',
    `chapter_title`  VARCHAR(200)  NOT NULL COMMENT '章节标题',
    `content`        LONGTEXT      NOT NULL COMMENT '章节正文',
    `word_count`     INT           NOT NULL DEFAULT 0 COMMENT '章节字数',
    `is_free`        TINYINT       NOT NULL DEFAULT 1 COMMENT '是否免费：0收费 1免费',
    `publish_status` TINYINT       NOT NULL DEFAULT 0 COMMENT '发布状态：0下架 1已发布',
    `published_at`   DATETIME      DEFAULT NULL COMMENT '发布时间',
    `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_book_chapter_no` (`book_id`, `chapter_no`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_publish_status` (`publish_status`),
    KEY `idx_published_at` (`published_at`),
    CONSTRAINT `fk_chapter_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节表';

-- -----------------------------------------------------------
-- 6. 用户书架表
-- -----------------------------------------------------------
CREATE TABLE `bookshelf` (
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '书架记录主键',
    `user_id`        BIGINT   NOT NULL COMMENT '用户ID',
    `book_id`        BIGINT   NOT NULL COMMENT '图书ID',
    `reading_status` TINYINT  NOT NULL DEFAULT 0 COMMENT '阅读状态：0未开始 1阅读中 2已读完 3暂停',
    `last_read_at`   DATETIME DEFAULT NULL COMMENT '最后阅读时间',
    `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_book` (`user_id`, `book_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_last_read_at` (`last_read_at`),
    CONSTRAINT `fk_bookshelf_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bookshelf_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户书架表';

-- -----------------------------------------------------------
-- 7. 阅读进度表（每用户每本书仅一条）
-- -----------------------------------------------------------
CREATE TABLE `reading_progress` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '进度主键',
    `user_id`          BIGINT        NOT NULL COMMENT '用户ID',
    `book_id`          BIGINT        NOT NULL COMMENT '图书ID',
    `chapter_id`       BIGINT        NOT NULL COMMENT '当前章节ID',
    `chapter_offset`   INT           NOT NULL DEFAULT 0 COMMENT '章节内阅读偏移（像素或字符位置）',
    `progress_percent` DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '阅读进度百分比',
    `last_read_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_book_progress` (`user_id`, `book_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_chapter_id` (`chapter_id`),
    CONSTRAINT `fk_progress_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_progress_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_progress_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读进度表';

-- -----------------------------------------------------------
-- 8. 阅读历史表（可保存多条记录）
-- -----------------------------------------------------------
CREATE TABLE `reading_history` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '历史记录主键',
    `user_id`          BIGINT   NOT NULL COMMENT '用户ID',
    `book_id`          BIGINT   NOT NULL COMMENT '图书ID',
    `chapter_id`       BIGINT   NOT NULL COMMENT '章节ID',
    `duration_seconds` INT      NOT NULL DEFAULT 0 COMMENT '阅读时长（秒）',
    `read_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_chapter_id` (`chapter_id`),
    KEY `idx_read_at` (`read_at`),
    KEY `idx_user_read_at` (`user_id`, `read_at`),
    CONSTRAINT `fk_history_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_history_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_history_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读历史表';

-- -----------------------------------------------------------
-- 9. 评论表（支持回复，parent_id 自关联）
-- -----------------------------------------------------------
CREATE TABLE `book_comment` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论主键',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `book_id`    BIGINT   NOT NULL COMMENT '图书ID',
    `parent_id`  BIGINT   DEFAULT NULL COMMENT '父评论ID，NULL为一级评论',
    `content`    TEXT     NOT NULL COMMENT '评论内容',
    `score`      TINYINT  DEFAULT NULL COMMENT '评分1-5，仅一级评论有效',
    `like_count` INT      NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status`     TINYINT  NOT NULL DEFAULT 1 COMMENT '状态：0屏蔽 1正常 2待审核',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_book_status` (`book_id`, `status`),
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `book_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评论表';
