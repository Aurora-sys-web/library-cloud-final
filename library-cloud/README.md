# 分布式图书馆管理系统 (Library-Cloud)

## 项目概述

这是一个基于 Spring Cloud Alibaba 的分布式微服务图书馆管理系统，实现了图书借阅的完整业务流程。

## 技术架构

### 技术栈

| 分类           | 技术                        |
| -------------- | --------------------------- |
| 核心框架       | Spring Boot 2.6.1           |
| 微服务框架     | Spring Cloud Alibaba 2021.1 |
| 服务注册与发现 | Alibaba Nacos               |
| 服务调用       | OpenFeign                   |
| 数据库         | MySQL 8.0                   |
| ORM框架        | MyBatis-Plus 3.5.2          |
| API文档        | SpringDoc OpenAPI 1.6.15    |
| 构建工具       | Maven                       |
| JDK版本        | Java 8                      |

### 项目结构

```
library-cloud/
├── common/                 # 公共模块
│   └── src/main/java/com/library/common/
│       ├── config/         # 配置类
│       │   ├── CorsConfig.java        # 跨域配置
│       │   ├── MybatisPlusConfig.java # MyBatis-Plus 配置
│       │   └── SwaggerConfig.java     # Swagger 配置
│       └── Result.java    # 统一响应结果
│
├── user-service/          # 用户服务 (端口: 8081)
│   └── src/main/java/com/library/user/
│       ├── controller/
│       │   └── UserController.java    # 用户管理接口
│       ├── entity/
│       │   └── User.java
│       ├── mapper/
│       │   └── UserMapper.java
│       └── service/
│           └── UserService.java
│
├── book-service/          # 图书服务 (端口: 8082)
│   └── src/main/java/com/library/book/
│       ├── controller/
│       │   └── BookController.java    # 图书管理接口
│       ├── entity/
│       │   └── Book.java
│       ├── mapper/
│       │   └── BookMapper.java
│       └── service/
│           └── BookService.java
│
├── borrow-service/        # 借阅服务 (端口: 8083)
│   └── src/main/java/com/library/borrow/
│       ├── controller/
│       │   ├── BorrowController.java       # 借阅管理
│       │   ├── BookWithUserController.java # 用户借阅查询
│       │   ├── DashboardController.java     # 仪表盘统计
│       │   └── LendRecordController.java    # 借阅记录
│       ├── entity/
│       │   ├── BookWithUser.java
│       │   └── LendRecord.java
│       ├── feign/
│       │   ├── BookFeignClient.java  # 图书服务调用
│       │   └── UserFeignClient.java  # 用户服务调用
│       ├── mapper/
│       │   ├── BookWithUserMapper.java
│       │   └── LendRecordMapper.java
│       └── service/
│           └── BorrowService.java
│
├── sql/                   # 数据库脚本
│   ├── library_user.sql   # 用户数据库
│   ├── library_book.sql   # 图书数据库
│   └── library_borrow.sql # 借阅数据库
│
└── vue/                   # 前端项目
    └── src/views/
        ├── Login.vue      # 登录页面
        ├── Register.vue   # 注册页面
        ├── Book.vue       # 图书管理
        ├── User.vue       # 用户管理
        ├── Dashboard.vue  # 仪表盘
        └── ...
```

## 功能模块

### 1. 用户服务 (user-service)

| 功能         | 接口                     | 说明             |
| ------------ | ------------------------ | ---------------- |
| 用户登录     | POST /user/login         | 用户身份验证     |
| 用户注册     | POST /user/register      | 新用户注册       |
| 获取用户     | GET /user/{id}           | 根据ID查询用户   |
| 更新用户     | PUT /user/update         | 更新用户信息     |
| 授予借阅权限 | PUT /user/alow/{id}      | 管理员授权       |
| 检查借阅权限 | GET /user/checkAlow/{id} | 验证用户借阅资格 |
| 分页查询用户 | GET /user/page           | 多条件分页查询   |
| 批量禁用用户 | POST /user/deleteBatch   | 批量操作         |
| 重置密码     | POST /user/resetPassword | 忘记密码重置     |

### 2. 图书服务 (book-service)

| 功能         | 接口                     | 说明           |
| ------------ | ------------------------ | -------------- |
| 获取图书列表 | GET /book/list           | 查询所有图书   |
| 分页查询图书 | GET /book/page           | 多条件分页查询 |
| 获取图书详情 | GET /book/{id}           | 根据ID查询     |
| 新增图书     | POST /book/save          | 添加新图书     |
| 更新图书     | PUT /book/update         | 修改图书信息   |
| 删除图书     | DELETE /book/{id}        | 删除图书       |
| 批量删除图书 | POST /book/deleteBatch   | 批量删除       |
| 图书分类统计 | GET /book/category/count | 分类统计       |

### 3. 借阅服务 (borrow-service)

| 功能           | 接口                         | 说明           |
| -------------- | ---------------------------- | -------------- |
| 借阅图书       | POST /borrow/borrow          | 用户借书       |
| 归还图书       | PUT /borrow/return/{id}      | 归还图书       |
| 查询借阅状态   | GET /borrow/user/{userId}    | 用户借阅查询   |
| 图书借阅统计   | GET /borrow/book/{bookId}    | 图书借出统计   |
| 管理员借阅查询 | GET /borrow/bookwithuser     | 多条件查询     |
| 借阅记录       | GET /borrow/record           | 借阅历史记录   |
| 仪表盘统计     | GET /borrow/dashboard/stats  | 首页统计数据   |
| 配置查询       | GET /borrow/config/maxBorrow | 最大借阅数配置 |

## 服务调用关系

```
┌─────────────────────────────────────────────────────────────┐
│                         前端 (Vue)                          │
│                      http://localhost:9876                 │
└─────────────────────────┬───────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│ user-service  │  │ book-service  │  │borrow-service │
│   (8081)      │  │    (8082)     │  │    (8083)     │
└───────────────┘  └───────────────┘  └───────────────┘
                           │                 │
                           │    OpenFeign    │
                           │◄────────────────┘
                           │
                    ┌──────┴──────┐
                    │   Nacos     │
                    │  (8848)     │
                    └─────────────┘
```

## 微服务调用

borrow-service 通过 OpenFeign 调用其他服务：

**UserFeignClient.java**

```java
@FeignClient(name = "user-service", path = "/user")
public interface UserFeignClient {
    @GetMapping("/{id}")
    Result<?> getUserById(@PathVariable("id") Long id);

    @GetMapping("/checkAlow/{id}")
    Result<?> checkAlow(@PathVariable("id") Long id);
}
```

**BookFeignClient.java**

```java
@FeignClient(name = "book-service", path = "/book")
public interface BookFeignClient {
    @GetMapping("/{id}")
    Result<?> getBookById(@PathVariable("id") Long id);

    @PutMapping("/borrow/{id}")
    Result<?> borrowBook(@PathVariable("id") Long id);
}
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Nacos 2.x
- Node.js 14+ (前端)

### 1. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE library_user;
CREATE DATABASE library_book;
CREATE DATABASE library_borrow;

-- 执行SQL脚本
source sql/library_user.sql;
source sql/library_book.sql;
source sql/library_borrow.sql;
```

### 2. 启动 Nacos

```bash
cd nacos/bin
startup.cmd -m standalone
```

访问 http://localhost:8848/nacos (默认账号: nacos/nacos)

### 3. 启动后端服务

按顺序启动：

1. **user-service** (端口 8081)
2. **book-service** (端口 8082)
3. **borrow-service** (端口 8083)

```bash
cd library-cloud
mvn clean install -DskipTests
# 在IDE中启动各服务
```

### 4. 启动前端

```bash
cd vue
npm install
npm run serve
```

访问 http://localhost:9876

## API 文档

启动服务后访问 Swagger UI：

| 服务           | Swagger 地址                          |
| -------------- | ------------------------------------- |
| user-service   | http://localhost:8081/swagger-ui.html |
| book-service   | http://localhost:8082/swagger-ui.html |
| borrow-service | http://localhost:8083/swagger-ui.html |

## 项目特性

### 1. 服务注册与发现

- 使用 Alibaba Nacos 作为服务注册中心
- 所有服务启动后自动注册到 Nacos
- 支持服务健康检查

### 2. 统一响应格式

```json
{
  "code": "0",
  "msg": "success",
  "data": {}
}
```

### 3. 跨域配置

- 后端统一配置 CORS
- 支持前端开发环境跨域访问

### 4. MyBatis-Plus 增强

- 自动填充创建时间、更新时间
- 分页插件自动处理
- 逻辑删除支持

## 默认账号

| 角色     | 用户名 | 密码     |
| -------- | ------ | -------- |
| 管理员   | admin  | 123456    |
| 普通用户 | reader1  | 123456 |

## 项目截图

- 登录页面
- 仪表盘统计
- 图书管理
- 用户管理
- 借阅管理
