# 折页 / Zheye

一个面向个人写作者的全栈博客平台，包含 Vue 3 前台、文章阅读、留言互动、作者草稿工作台、内容管理、秘密基地和小游戏。正式后端为 Java Spring Boot + MySQL。

## 技术栈

- 前端：Vue 3、Vite、Vue Router、JavaScript、CSS、Markdown-it、highlight.js
- 后端：Java 21、Spring Boot、Spring Security、JWT、BCrypt、JdbcTemplate、MySQL 8
- 部署：GitHub Pages 静态前端 + 支持 Java 的云主机 + MySQL

## F 盘目录

项目文件、JDK、Maven 缓存和构建产物均应保留在 F 盘：

- 前端工作区：`F:\blog-projects\myblog`
- Java 后端：`F:\blog-projects\myblog-java-backend\backend-java`
- Maven 本地仓库建议：`F:\maven-repository`

不要把项目副本、数据库备份、Maven 缓存或构建目录放到 C 盘。运行前端所使用的 Node.js 仅是构建工具，不作为项目后端。

## 本机启动

### 1. 导入数据库

```powershell
mysql -u root -p < F:\blog-projects\myblog\database\schema.sql
mysql -u root -p < F:\blog-projects\myblog\database\seed.sql
```

### 2. 在 IntelliJ IDEA 启动 Java 后端

在 IDEA 中打开 `F:\blog-projects\myblog-java-backend\backend-java`，选择 F 盘的 JDK 21，并在运行配置中设置：

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=myblog
DB_USER=root
DB_PASSWORD=你的数据库密码
JWT_SECRET=至少32位的随机字符串
FRONTEND_URL=http://localhost:5173
PORT=3000
```

运行 `MyBlogApplication`。服务地址为 `http://localhost:3000`，健康检查为 `GET /api/health`。

命令行运行时，Maven 缓存指定在 F 盘：

```powershell
mvn -Dmaven.repo.local=F:\maven-repository spring-boot:run
```

### 3. 启动前端

```powershell
cd F:\blog-projects\myblog\frontend
npm install
npm run dev
```

前端默认地址为 `http://localhost:5173`，API 默认地址为 `http://localhost:3000/api`。需要调整时，在 `frontend\.env` 中设置：

```text
VITE_API_BASE_URL=http://localhost:3000/api
```

## 已实现功能

- 注册、登录、退出、修改密码和登录门禁
- 文章浏览、归档、搜索、阅读进度、主题与阅读设置
- 留言与回复
- 作者草稿新建、编辑、删除和发布
- 作者资料、About、大事记、兴趣、友链的读取和作者编辑
- 秘密基地密码初始化、密码验证和作者内容管理
- 小游戏的开始、暂停、继续、重开、退出、倒计时和计分
- 移动端导航与响应式布局

公开内容编辑接口要求作者或管理员 JWT。除登录与注册页外，前端页面要求登录后访问；作者后台还会校验 `author` 或 `admin` 角色。

## 验证

```powershell
cd F:\blog-projects\myblog\frontend
npm run build

cd F:\blog-projects\myblog-java-backend\backend-java
mvn -Dmaven.repo.local=F:\maven-repository test
```

本机验收顺序：注册或登录作者账号，进入“作者后台 -> Content”，初始化秘密基地密码，填写 About，再在公开页面检查 About、大事记、兴趣、友链和秘密基地；最后进入小游戏检查暂停、继续、重开和退出。

## 部署

GitHub Pages 仅部署静态前端，构建环境中必须配置 `VITE_API_BASE_URL` 指向已上线的 Java API。Java 服务部署在支持 Java 21 与 MySQL 的云主机，使用环境变量注入数据库账号、JWT 密钥和 `FRONTEND_URL`；不要提交 `.env`、真实密码或密钥。
