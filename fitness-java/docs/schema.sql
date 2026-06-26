-- ============================================
-- Fitness Platform Database Schema
-- MySQL 8.0
-- ============================================

CREATE DATABASE IF NOT EXISTS fitness
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE fitness;

-- ============================================
-- User table
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `username`    VARCHAR(50)  NOT NULL UNIQUE,
    `password`    VARCHAR(255) NOT NULL,
    `nickname`    VARCHAR(50)  NOT NULL,
    `avatar_url`  VARCHAR(500),
    `phone`       VARCHAR(20),
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=disabled, 1=active',
    `bio`         VARCHAR(500) COMMENT 'coach profile: biography',
    `certifications` VARCHAR(500) COMMENT 'coach profile: certifications',
    `specialties` VARCHAR(300) COMMENT 'coach profile: specialties',
    `experience`  INT          COMMENT 'coach profile: years of experience',
    `hourly_rate` DECIMAL(10,2) COMMENT 'coach profile: hourly rate',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (`username`),
    INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Course table
-- ============================================
CREATE TABLE IF NOT EXISTS `course` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `coach_id`    BIGINT       NOT NULL,
    `title`       VARCHAR(200) NOT NULL,
    `description` TEXT,
    `cover_url`   VARCHAR(500),
    `video_url`   VARCHAR(500),
    `category`    VARCHAR(50)  NOT NULL,
    `difficulty`  VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    `duration`    INT          COMMENT 'minutes',
    `price`        DECIMAL(10,2) DEFAULT 0,
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    `rating`       DECIMAL(2,1) DEFAULT 0,
    `rating_count` INT          DEFAULT 0,
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coach (`coach_id`),
    INDEX idx_category (`category`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Course exercises (videos + text per course)
-- ============================================
CREATE TABLE IF NOT EXISTS `course_exercise` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `course_id`   BIGINT       NOT NULL,
    `title`       VARCHAR(200) NOT NULL,
    `video_url`   VARCHAR(500),
    `description` TEXT,
    `sort_order`  INT          DEFAULT 0,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- User-Course subscription
-- ============================================
CREATE TABLE IF NOT EXISTS `user_course` (
    `id`            BIGINT   AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT   NOT NULL,
    `course_id`     BIGINT   NOT NULL,
    `progress`      INT      DEFAULT 0 COMMENT '0-100',
    `purchase_time` DATETIME,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_course (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Training records
-- ============================================
CREATE TABLE IF NOT EXISTS `training_record` (
    `id`          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT      NOT NULL,
    `course_id`   BIGINT,
    `duration`    INT         COMMENT 'minutes',
    `calories`    INT,
    `notes`       VARCHAR(500),
    `record_date` DATE        NOT NULL,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Diet records
-- ============================================
CREATE TABLE IF NOT EXISTS `diet_record` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT       NOT NULL,
    `meal_type`   VARCHAR(50)  NOT NULL,
    `food_id`     BIGINT       COMMENT 'FK to food_item, null=manual entry',
    `weight_grams` DECIMAL(7,1) COMMENT 'actual weight eaten in grams',
    `food_name`   VARCHAR(100) NOT NULL,
    `calories`    INT,
    `protein`     DECIMAL(5,1) DEFAULT 0,
    `carbs`       DECIMAL(5,1) DEFAULT 0,
    `fat`         DECIMAL(5,1) DEFAULT 0,
    `image_url`   VARCHAR(500),
    `record_date` DATE         NOT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Body metrics
-- ============================================
CREATE TABLE IF NOT EXISTS `body_metrics` (
    `id`          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT      NOT NULL,
    `weight`      DECIMAL(5,1) COMMENT 'kg',
    `body_fat`    DECIMAL(4,1) COMMENT '%',
    `bmi`         DECIMAL(4,1),
    `waist`       DECIMAL(5,1) COMMENT 'cm',
    `hip`         DECIMAL(5,1) COMMENT 'cm',
    `record_date` DATE        NOT NULL,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Training plans (coach -> user)
-- ============================================
CREATE TABLE IF NOT EXISTS `training_plan` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `coach_id`    BIGINT       NOT NULL,
    `user_id`     BIGINT       NOT NULL,
    `title`       VARCHAR(200) NOT NULL,
    `content`     TEXT         COMMENT 'JSON structured plan',
    `start_date`  DATE,
    `end_date`    DATE,
    `status`      TINYINT      DEFAULT 0 COMMENT '0=in-progress, 1=completed, 2=expired',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coach (`coach_id`),
    INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Operation logs
-- ============================================
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT,
    `username`    VARCHAR(50),
    `action`      VARCHAR(50)  NOT NULL COMMENT 'LOGIN, UPDATE_ROLE, CREATE_USER, etc.',
    `target`      VARCHAR(200) COMMENT 'target resource name',
    `detail`      VARCHAR(500) COMMENT 'extra detail',
    `ip`          VARCHAR(50),
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (`user_id`),
    INDEX idx_action (`action`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Member cards
-- ============================================
CREATE TABLE IF NOT EXISTS `member_card` (
    `id`            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `name`          VARCHAR(100) NOT NULL,
    `duration_days` INT          NOT NULL COMMENT 'valid days',
    `price`         DECIMAL(10,2) NOT NULL DEFAULT 0,
    `description`   VARCHAR(500),
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0=disabled, 1=active',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Group class scheduling
-- ============================================
CREATE TABLE IF NOT EXISTS `group_class` (
    `id`                 BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `course_id`          BIGINT      NOT NULL,
    `coach_id`           BIGINT      NOT NULL,
    `start_time`         DATETIME    NOT NULL,
    `end_time`           DATETIME    NOT NULL,
    `max_capacity`       INT         NOT NULL DEFAULT 20,
    `current_enrollment` INT         DEFAULT 0,
    `location`           VARCHAR(100),
    `status`             VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    `create_time`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (`course_id`),
    INDEX idx_coach (`coach_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Coach-student bindings (1v1 PT)
-- ============================================
CREATE TABLE IF NOT EXISTS `coach_student` (
    `id`          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `coach_id`    BIGINT      NOT NULL,
    `student_id`  BIGINT      NOT NULL,
    `status`      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `start_date`  DATE,
    `end_date`    DATE,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coach (`coach_id`),
    INDEX idx_student (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Coach weekly shifts
-- ============================================
CREATE TABLE IF NOT EXISTS `coach_shift` (
    `id`          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    `coach_id`    BIGINT   NOT NULL,
    `day_of_week` INT      NOT NULL COMMENT '1=Monday, 7=Sunday',
    `start_time`  TIME     NOT NULL,
    `end_time`    TIME     NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coach_day (`coach_id`, `day_of_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- RBAC: Role / Permission / Role-Permission
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(50)  NOT NULL,
    `code`        VARCHAR(50)  NOT NULL UNIQUE,
    `description` VARCHAR(200),
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(50)  NOT NULL,
    `code`        VARCHAR(50)  NOT NULL UNIQUE,
    `group_name`  VARCHAR(50)  NOT NULL,
    `description` VARCHAR(200),
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id`            BIGINT   AUTO_INCREMENT PRIMARY KEY,
    `role_id`       BIGINT   NOT NULL,
    `permission_id` BIGINT   NOT NULL,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_role_perm (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Seed data: default roles
-- ============================================
INSERT IGNORE INTO `sys_role` (`code`, `name`, `description`) VALUES
('ADMIN', 'System Admin', 'Full system access'),
('COACH', 'Coach',       'Course and student management'),
('USER',  'User',        'Basic user access');

-- ============================================
-- ============================================
-- Seed data: default permissions
-- ============================================
INSERT IGNORE INTO `sys_permission` (`code`, `name`, `group_name`, `description`) VALUES
('user:view',     'View Users',     'User Management',    'View user list and details'),
('user:edit',     'Edit Users',     'User Management',    'Modify user roles and status'),
('user:create',   'Create Users',   'User Management',    'Create new user accounts'),
('course:view',   'View Courses',   'Course Management',  'View course list'),
('course:approve','Approve Courses','Course Management',  'Approve or reject courses'),
('course:schedule','Schedule Class','Course Management',  'Create and edit class schedule'),
('coach:view',    'View Coaches',   'Coach Management',   'View coach profiles and shifts'),
('coach:edit',    'Edit Coaches',   'Coach Management',   'Edit coach info and profiles'),
('settings:view', 'View Settings',  'System Settings',    'View system settings'),
('settings:edit', 'Edit Settings',  'System Settings',    'Modify system settings and permissions');

-- ============================================
-- Food item database (USDA cache + local presets)
-- ============================================
CREATE TABLE IF NOT EXISTS `food_item` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `fdc_id`      INT          COMMENT 'USDA FDC ID (null for local/custom)',
    `name`        VARCHAR(200) NOT NULL,
    `name_en`     VARCHAR(200) COMMENT 'English name for USDA search',
    `category`    VARCHAR(50)  COMMENT '主食/肉类/蔬菜/水果/乳制品/蛋类/豆制品/零食/饮品/调料',
    `calories`    DECIMAL(7,2) COMMENT 'kcal per 100g',
    `protein`     DECIMAL(5,2) COMMENT 'g per 100g',
    `carbs`       DECIMAL(5,2) COMMENT 'g per 100g',
    `fat`         DECIMAL(5,2) COMMENT 'g per 100g',
    `source`      VARCHAR(20)  DEFAULT 'PRESET' COMMENT 'USDA/PRESET/CUSTOM',
    `is_common`   TINYINT      DEFAULT 0 COMMENT '1=common food, shown first',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_fdc_id (`fdc_id`),
    INDEX idx_name (`name`),
    INDEX idx_category (`category`),
    INDEX idx_source (`source`),
    INDEX idx_is_common (`is_common`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- User custom meal types
-- ============================================
CREATE TABLE IF NOT EXISTS `user_meal_type` (
    `id`          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT      NOT NULL,
    `name`        VARCHAR(50) NOT NULL,
    `code`        VARCHAR(50) NOT NULL COMMENT 'MEAL_1, MEAL_2, etc.',
    `sort_order`  INT         DEFAULT 0,
    `is_active`   TINYINT     DEFAULT 1 COMMENT '0=disabled, 1=enabled',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uq_user_code (`user_id`, `code`),
    INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Diet record extensions (run if table already exists)
-- ============================================
-- ALTER TABLE `diet_record`
--   ADD COLUMN `food_id`      BIGINT       COMMENT 'FK to food_item, null=manual entry',
--   ADD COLUMN `weight_grams` DECIMAL(7,1) COMMENT 'actual weight eaten in grams';
