# 折页 / Zheye

一个面向个人写作者的全栈博客平台。首版包含 Vue 3 前台、阅读体验、留言互动、作者草稿工作台、小游戏和 Express + MySQL API，可继续扩展为生产部署版本。

## 技术栈

- 前端：Vue 3、Vite、Vue Router、JavaScript、CSS、Markdown-it、highlight.js
- 后端：Node.js、Express、JWT、bcryptjs、Zod、MySQL2
- 部署：GitHub Pages 静态前端 + 任意支持 Node.js 的免费云主机 + MySQL

## 快速开始

```bash
npm run install:all
copy frontend\.env.example frontend\.env
copy backend\.env.example backend\.env
npm run dev
```

前端默认 `http://localhost:5173`，后端默认 `http://localhost:3000`。没有 MySQL 时，前台仍可使用内置本地内容；连接数据库后通过 `/api` 接入真实数据。

## 数据库

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

请在 `backend/.env` 中配置 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 和随机 `JWT_SECRET`。生产环境只通过环境变量注入密钥，不提交 `.env`。备份：`mysqldump -u root -p myblog > backup.sql`；恢复：`mysql -u root -p myblog < backup.sql`。

## 已落地的用例

前台已落地首页浏览、文章列表/详情、Markdown 与代码高亮、归档筛选、关键词筛选、阅读进度、主题切换、阅读字号、留言发布、About、小游戏、响应式导航和 Hash 路由。后台已落地作者权限入口、新建草稿、编辑、保存状态、删除和基础风险提示。后端已落地统一响应格式、注册、登录、JWT 当前用户、文章列表/详情、参数化查询、CORS、限流和 MySQL 表结构，`schema.sql` 覆盖用户、文章、草稿、标签、留言回复、私密内容、个人资料、时间线、兴趣、友链、阅读统计、热力记录、找回密码记录和登录日志。

其余接口按同样的 controller/service/routes 边界继续补充即可，数据库字段已经预留。当前演示登录中输入账号 `author` 可进入作者后台；真实环境应使用 seed 中的 bcrypt 密码或重新初始化作者密码。

## GitHub Pages

仓库启用 Pages 的 GitHub Actions 部署，工作流位于 `.github/workflows/deploy-frontend.yml`，前端使用 Hash 路由避免刷新 404。部署前将仓库 Pages 设置为 GitHub Actions，并在构建环境配置 `VITE_API_BASE_URL`。

## 后端部署

在云主机上安装 Node.js 与 MySQL，上传 `backend` 和 `database`，导入 SQL，设置环境变量后运行：

```bash
npm install --omit=dev
npm start
```

使用云主机自带的进程守护、systemd 或 PM2 保持服务重启后自动运行；HTTPS 建议由 Nginx/Caddy 终止，`FRONTEND_URL` 仅填写实际 GitHub Pages 域名。免费主机的 CPU、内存、休眠和数据库容量限制取决于供应商，生产使用前请确认其条款。

## 验证

```bash
npm run build
```

API 健康检查：`GET /api/health`。核心页面无需登录可直接访问；作者后台会校验本地角色状态，后端接口使用 JWT 和角色中间件保护。网络、空数据和无权限状态均有前端提示或空状态呈现。
