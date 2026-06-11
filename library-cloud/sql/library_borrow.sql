CREATE DATABASE IF NOT EXISTS library_borrow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_borrow;

DROP TABLE IF EXISTS `lend_record`;
CREATE TABLE `lend_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `reader_id` bigint NOT NULL,
    `isbn` varchar(255) NOT NULL,
    `bookname` varchar(255) DEFAULT NULL,
    `lend_time` datetime DEFAULT NULL,
    `return_time` datetime DEFAULT NULL,
    `status` varchar(1) DEFAULT '0',
    `borrownum` int DEFAULT 0,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `bookwithuser`;
CREATE TABLE `bookwithuser` (
    `id` bigint NOT NULL,
    `isbn` varchar(255) DEFAULT NULL,
    `book_name` varchar(255) NOT NULL,
    `nick_name` varchar(255) DEFAULT NULL,
    `lendtime` datetime DEFAULT NULL,
    `deadtime` datetime DEFAULT NULL,
    `prolong` int DEFAULT 1,
    PRIMARY KEY (`id`, `isbn`)
);