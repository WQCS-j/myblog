package com.myblog.draft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.common.ApiResponse;
import com.myblog.common.RowMapperUtil;
import com.myblog.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Statement;
import java.text.Normalizer;
import java.util.*;

@RestController
@RequestMapping("/api/drafts")
@PreAuthorize("hasAnyRole('author','admin')")
public class DraftController {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DraftController(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "updatedAt") String order
    ) {
        String search = "%" + keyword.trim() + "%";
        String orderBy = "createdAt".equals(order) ? "created_at" : "updated_at";
        return ApiResponse.ok(jdbcTemplate.queryForList("SELECT id, title, summary, content, author_id, category_id, status, risk_info, created_at, updated_at FROM drafts WHERE author_id = ? AND status = 'draft' AND (title LIKE ? OR summary LIKE ?) ORDER BY " + orderBy + " DESC", principal.userId(), search, search).stream().map(this::toDraft).toList());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody DraftRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO drafts (title, summary, content, author_id, category_id, status, risk_info) VALUES (?, ?, ?, ?, ?, 'draft', ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, value(request.title()));
            statement.setString(2, value(request.summary()));
            statement.setString(3, value(request.content()));
            statement.setLong(4, principal.userId());
            statement.setObject(5, request.categoryId());
            statement.setString(6, riskInfo(request.riskInfo()));
            return statement;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        if (generatedId == null) throw new IllegalStateException("草稿创建失败");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(findOwned(principal.userId(), generatedId.longValue()), "草稿已创建"));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable long id) { return ApiResponse.ok(findOwned(principal.userId(), id)); }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable long id, @RequestBody DraftRequest request) {
        int updatedRows = jdbcTemplate.update("UPDATE drafts SET title = ?, summary = ?, content = ?, category_id = ?, risk_info = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND author_id = ? AND status = 'draft'", value(request.title()), value(request.summary()), value(request.content()), request.categoryId(), riskInfo(request.riskInfo()), id, principal.userId());
        if (updatedRows != 1) throw new NoSuchElementException("草稿不存在或无权修改");
        return ApiResponse.ok(findOwned(principal.userId(), id), "草稿已保存");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable long id) {
        int deletedRows = jdbcTemplate.update("DELETE FROM drafts WHERE id = ? AND author_id = ? AND status = 'draft'", id, principal.userId());
        if (deletedRows != 1) throw new NoSuchElementException("草稿不存在或无权删除");
        return ApiResponse.ok(null, "草稿已删除");
    }

    @PostMapping("/{id}/publish")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> publish(@AuthenticationPrincipal UserPrincipal principal, @PathVariable long id) {
        Map<String, Object> draft = findOwnedDraft(principal.userId(), id);
        String title = value(draft.get("title")).trim();
        String content = value(draft.get("content")).trim();
        if (title.length() < 2) throw new IllegalArgumentException("标题至少需要 2 个字");
        if (content.isEmpty()) throw new IllegalArgumentException("正文不能为空");

        int publishedRows = jdbcTemplate.update("UPDATE drafts SET status = 'published', updated_at = CURRENT_TIMESTAMP WHERE id = ? AND author_id = ? AND status = 'draft'", id, principal.userId());
        if (publishedRows != 1) throw new NoSuchElementException("草稿不存在或无权发布");

        String slug = makeSlug(title + "-" + System.currentTimeMillis());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("INSERT INTO articles (title, slug, summary, content, author_id, category_id, status, published_at) VALUES (?, ?, ?, ?, ?, ?, 'published', CURRENT_TIMESTAMP)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setString(2, slug);
            statement.setString(3, value(draft.get("summary")));
            statement.setString(4, content);
            statement.setLong(5, principal.userId());
            statement.setObject(6, draft.get("categoryId"));
            return statement;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        if (generatedId == null) throw new IllegalStateException("文章发布失败");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("articleId", generatedId.longValue());
        result.put("slug", slug);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result, "文章已发布"));
    }

    private Map<String, Object> findOwned(long userId, long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, title, summary, content, author_id, category_id, status, risk_info, created_at, updated_at FROM drafts WHERE id = ? AND author_id = ?", id, userId);
        if (rows.isEmpty()) throw new NoSuchElementException("草稿不存在或无权访问");
        return toDraft(rows.getFirst());
    }

    private Map<String, Object> findOwnedDraft(long userId, long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, title, summary, content, author_id, category_id, status, risk_info, created_at, updated_at FROM drafts WHERE id = ? AND author_id = ? AND status = 'draft'", id, userId);
        if (rows.isEmpty()) throw new NoSuchElementException("草稿不存在或无权发布");
        return toDraft(rows.getFirst());
    }

    private Map<String, Object> toDraft(Map<String, Object> row) {
        Map<String, Object> result = RowMapperUtil.camel(row);
        Object rawRiskInfo = result.get("riskInfo");
        if (rawRiskInfo instanceof String riskInfo) {
            try { result.put("riskInfo", objectMapper.readTree(riskInfo)); }
            catch (JsonProcessingException ignored) { result.put("riskInfo", objectMapper.createObjectNode()); }
        }
        if (rawRiskInfo == null) result.put("riskInfo", objectMapper.createObjectNode());
        return result;
    }

    private String value(String value) { return value == null ? "" : value; }
    private String value(Object value) { return value == null ? "" : String.valueOf(value); }
    private String riskInfo(JsonNode value) { return value == null || value.isNull() ? "{}" : value.toString(); }
    private String makeSlug(String value) { return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFKD).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""); }

    public record DraftRequest(String title, String summary, String content, Integer categoryId, JsonNode riskInfo) {}
}
