CREATE DATABASE IF NOT EXISTS library_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_user;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(255) DEFAULT NULL,
    `password` varchar(255) DEFAULT NULL,
    `nick_name` varchar(255) DEFAULT NULL,
    `phone` varchar(255) DEFAULT NULL,
    `sex` varchar(255) DEFAULT NULL,
    `address` varchar(255) DEFAULT NULL,
    `role` int NOT NULL DEFAULT 2,
    `alow` varchar(1) DEFAULT '0',
    PRIMARY KEY (`id`)
);

INSERT INTO `user` VALUES (1, 'admin', '123456', '管理员', '13800138000', '男', '图书馆', 1, '1');
INSERT INTO `user` VALUES (2, 'reader1', '123456', '张三', '13800138001', '男', '北京市朝阳区', 2, '1');
INSERT INTO `user` VALUES (3, 'reader2', '123456', '李四', '13800138002', '女', '上海市浦东新区', 2, '0');