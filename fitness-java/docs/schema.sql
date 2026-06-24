-- Fitness Platform Database Schema
-- MySQL 8.0

CREATE DATABASE IF NOT EXISTS fitness DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitness;

-- User table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `avatar_url` VARCHAR(500),
    `phone` VARCHAR(20),
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled, 1-active',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (`username`),
    INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Course table
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `coach_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `cover_url` VARCHAR(500),
    `video_url` VARCHAR(500),
    `category` VARCHAR(50) NOT NULL,
    `difficulty` VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    `duration` INT COMMENT 'minutes',
    `price` DECIMAL(10,2) DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coach (`coach_id`),
    INDEX idx_category (`category`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Course subscription
CREATE TABLE IF NOT EXISTS `user_course` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `progress` INT DEFAULT 0 COMMENT '0-100',
    `purchase_time` DATETIME,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_course (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Training records
CREATE TABLE IF NOT EXISTS `training_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `course_id` BIGINT,
    `duration` INT COMMENT 'minutes',
    `calories` INT,
    `notes` VARCHAR(500),
    `record_date` DATE NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Diet records
CREATE TABLE IF NOT EXISTS `diet_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `meal_type` VARCHAR(20) NOT NULL,
    `food_name` VARCHAR(100) NOT NULL,
    `calories` INT,
    `protein` DECIMAL(5,1) DEFAULT 0,
    `carbs` DECIMAL(5,1) DEFAULT 0,
    `fat` DECIMAL(5,1) DEFAULT 0,
    `image_url` VARCHAR(500),
    `record_date` DATE NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Body metrics
CREATE TABLE IF NOT EXISTS `body_metrics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `weight` DECIMAL(5,1) COMMENT 'kg',
    `body_fat` DECIMAL(4,1) COMMENT '%',
    `bmi` DECIMAL(4,1),
    `waist` DECIMAL(5,1) COMMENT 'cm',
    `hip` DECIMAL(5,1) COMMENT 'cm',
    `record_date` DATE NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Training plans (coach -> user)
CREATE TABLE IF NOT EXISTS `training_plan` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `coach_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT COMMENT 'JSON structured plan',
    `start_date` DATE,
    `end_date` DATE,
    `status` TINYINT DEFAULT 0 COMMENT '0-in-progress, 1-completed, 2-expired',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coach (`coach_id`),
    INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
