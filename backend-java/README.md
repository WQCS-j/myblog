# Java 后端

这是 MyBlog 的正式后端，使用 Java 21、Spring Boot 3、Spring Security、JWT、BCrypt、JdbcTemplate 和 MySQL。

## 启动

```bash
mvn spring-boot:run
```

打包并启动：

```bash
mvn clean package -DskipTests
java -jar target/myblog-backend-java-1.0.0.jar
```

运行前配置 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`、`JWT_SECRET`、`FRONTEND_URL` 和 `PORT`。API 路径保持为 `/api`，用于兼容现有 Vue 前端。
