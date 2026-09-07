package com.myblog.article;

import com.myblog.common.ApiResponse;
import com.myblog.common.RowMapperUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ArticleController {
    private final JdbcTemplate jdbcTemplate;
    public ArticleController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping({"/articles", "/articles/search"})
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer limit) {
        int requestedSize = limit != null ? limit : (pageSize != null ? pageSize : 10);
        int safePage = Math.max(page, 1), safeSize = Math.min(Math.max(requestedSize, 1), 50), offset = (safePage - 1) * safeSize;
        String search = "%" + keyword.trim() + "%";
        StringBuilder where = new StringBuilder(" WHERE a.status = 'published' AND (a.title LIKE ? OR a.summary LIKE ? OR a.content LIKE ?)");
        List<Object> args = new ArrayList<>(List.of(search, search, search));
        if (categoryId != null) { where.append(" AND a.category_id = ?"); args.add(categoryId); }
        if (category != null && !category.isBlank()) { where.append(" AND c.name = ?"); args.add(category.trim()); }
        if (tag != null && !tag.isBlank()) { where.append(" AND EXISTS (SELECT 1 FROM article_tags at JOIN tags t ON t.id = at.tag_id WHERE at.article_id = a.id AND t.name = ?)"); args.add(tag); }
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM articles a LEFT JOIN categories c ON c.id = a.category_id" + where, Integer.class, args.toArray());
        args.add(safeSize); args.add(offset);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT a.id, a.title, a.slug, a.summary, a.content, a.author_id, a.category_id, a.status, a.published_at, a.created_at, a.updated_at, u.username AS author, c.name AS category FROM articles a JOIN users u ON u.id = a.author_id LEFT JOIN categories c ON c.id = a.category_id" + where + " ORDER BY a.published_at DESC, a.id DESC LIMIT ? OFFSET ?", args.toArray());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", rows.stream().map(RowMapperUtil::camel).toList());
        data.put("total", total == null ? 0 : total);
        data.put("page", safePage);
        data.put("limit", safeSize);
        data.put("pageSize", safeSize);
        data.put("keyword", keyword.trim());
        data.put("category", category == null ? "" : category.trim());
        data.put("tag", tag == null ? "" : tag.trim());
        return ApiResponse.ok(data);
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable int id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT a.*, u.username AS author, c.name AS category FROM articles a JOIN users u ON u.id = a.author_id LEFT JOIN categories c ON c.id = a.category_id WHERE a.id = ? AND a.status = 'published'", id);
        if (rows.isEmpty()) throw new NoSuchElementException("文章不存在或尚未发布");
        Map<String, Object> result = RowMapperUtil.camel(rows.getFirst());
        result.put("tags", jdbcTemplate.queryForList("SELECT t.id, t.name FROM article_tags at JOIN tags t ON t.id = at.tag_id WHERE at.article_id = ?", id).stream().map(RowMapperUtil::camel).toList());
        return ApiResponse.ok(result);
    }

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> categories() { return ApiResponse.ok(jdbcTemplate.queryForList("SELECT id, name, created_at FROM categories ORDER BY name").stream().map(RowMapperUtil::camel).toList()); }
    @GetMapping("/tags")
    public ApiResponse<List<Map<String, Object>>> tags() { return ApiResponse.ok(jdbcTemplate.queryForList("SELECT id, name FROM tags ORDER BY name").stream().map(RowMapperUtil::camel).toList()); }

    @GetMapping("/archive")
    public ApiResponse<Map<String, Map<String, List<Map<String, Object>>>>> archive() {
        Map<String, Map<String, List<Map<String, Object>>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT id, title, published_at FROM articles WHERE status = 'published' ORDER BY published_at DESC")) {
            Map<String, Object> article = RowMapperUtil.camel(row);
            Object publishedAt = article.get("publishedAt");
            if (!(publishedAt instanceof java.time.LocalDateTime time)) continue;
            String year = String.valueOf(time.getYear());
            String month = "%02d".formatted(time.getMonthValue());
            groups.computeIfAbsent(year, ignored -> new LinkedHashMap<>()).computeIfAbsent(month, ignored -> new ArrayList<>()).add(article);
        }
        return ApiResponse.ok(groups);
    }
}