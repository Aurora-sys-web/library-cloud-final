CREATE DATABASE IF NOT EXISTS library_book DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_book;

DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `isbn` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `price` decimal(10,2) DEFAULT NULL,
    `author` varchar(255) DEFAULT NULL,
    `publisher` varchar(255) DEFAULT NULL,
    `create_time` date DEFAULT NULL,
    `status` varchar(1) NOT NULL DEFAULT '1',
    `borrownum` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

INSERT INTO `book` VALUES (1, '9787121321014', 'Java编程思想', 108.00, 'Bruce Eckel', '机械工业出版社', '2020-01-01', '1', 0);
INSERT INTO `book` VALUES (2, '9787115428028', 'Spring Boot实战', 89.00, 'Craig Walls', '人民邮电出版社', '2020-05-01', '1', 0);
INSERT INTO `book` VALUES (3, '9787121318762', '微服务设计', 79.00, 'Sam Newman', '机械工业出版社', '2019-10-01', '1', 0);