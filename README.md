# 折页 / Zheye

一个面向个人写作者的全栈博客平台。前端使用 Vue 3 + Vite，正式后端使用 Java 21 + Spring Boot + MySQL，支持文章浏览、用户认证、作者草稿、留言回复、私密内容和阅读数据分析。

## 技术栈

- 前端：Vue 3、Vite、Vue Router、JavaScript、CSS、Markdown-it、highlight.js
- 后端：Java 21、Spring Boot 3、Spring Security、JWT、BCrypt、JdbcTemplate
- 数据库：MySQL 8，结构位于 `database/schema.sql`，初始化数据位于 `database/seed.sql`
- 部署：GitHub Pages 静态前端 + 支持 Java 的云主机 + MySQL

## 目录

- `frontend`：Vue 前端
- `backend-java`：正式 Java/Spring Boot 后端
- `backend-java`：唯一后端，Java/Spring Boot REST API
- `database`：MySQL 表结构和种子脚本
- `.github/workflows`：GitHub Pages 自动部署

## 本地启动

环境要求：Node.js 20+、Java 21、Maven 3.9+、MySQL 8+。

```bash
npm install --prefix frontend
copy backend-java\.env.example backend-java\.env
npm run dev
```

上面的 `npm run dev` 启动前端，默认地址为 `http://localhost:5173`。另开一个终端启动 Java 后端：

```bash
mvn -f backend-java/pom.xml spring-boot:run
```

如果 Maven 没有加入 PATH，Windows 可以使用本机 Maven 的完整路径：

```powershell
D:\tools\apache-maven-3.9.16\bin\mvn.cmd -f backend-java\pom.xml spring-boot:run
```

也可以尝试同时启动：

```bash
npm run dev:all
```

Java API 默认运行在 `http://localhost:3000`，接口前缀为 `/api`。

## 数据库

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

在 `backend-java/.env` 或云主机环境变量中配置：

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=myblog
DB_USER=root
DB_PASSWORD=你的数据库密码
JWT_SECRET=至少32字符的随机密钥
FRONTEND_URL=http://localhost:5173
PORT=3000
```

不要提交真实 `.env`。数据库备份：

```bash
mysqldump -u root -p myblog > backup.sql
mysql -u root -p myblog < backup.sql
```

## 已实现的 Java API

- 认证：注册、登录、退出、当前用户、修改密码、JWT、BCrypt
- 文章：列表、详情、搜索、分类、标签、归档
- 作者后台：草稿列表、新建、详情、编辑、删除、发布
- 互动：留言、回复、回复查看、最新回复
- 个人内容：资料、时间线、兴趣、友情链接、私密内容验证
- 阅读数据：阅读记录、作者/管理员阅读热力统计
- 安全：CORS、参数化 SQL、角色权限、草稿归属校验、统一错误响应

没有预置他人文章，`seed.sql` 默认不写入演示文章、留言和草稿。作者可以从后台创建自己的第一篇文章。

## 构建和测试

```bash
npm run build
mvn -f backend-java/pom.xml clean package
mvn -f backend-java/pom.xml test
```

生产启动：

```bash
java -jar backend-java/target/myblog-backend-java-1.0.0.jar
```

## GitHub Pages

工作流位于 `.github/workflows/deploy-frontend.yml`。仓库 Pages 设置选择 GitHub Actions，前端使用 Hash 路由避免刷新 404。部署时配置 `VITE_API_BASE_URL`，例如：

```text
https://你的后端域名/api
```

后端的 `FRONTEND_URL` 必须填写实际 GitHub Pages 来源，用于 CORS。云主机需要配置 Java 21、MySQL、HTTPS 和进程守护；后端不依赖个人电脑持续开机。

## 迁移说明

当前正式后端只有 `backend-java`。旧 Node.js 后端目录已删除，部署、开发和 README 均以 Java/Spring Boot 为准。
