package com.myblog.content;

import com.myblog.common.ApiResponse;
import com.myblog.common.RowMapperUtil;
import com.myblog.security.UserPrincipal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ContentController {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public ContentController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, bio, skills, projects, contact, avatar_url FROM personal_profile ORDER BY id LIMIT 1");
        return ApiResponse.ok(rows.isEmpty() ? Map.of() : RowMapperUtil.camel(rows.getFirst()));
    }

    @GetMapping("/timeline")
    public ApiResponse<List<Map<String, Object>>> timeline() {
        return ApiResponse.ok(jdbcTemplate.queryForList("SELECT id, event_date, title, content FROM personal_timeline ORDER BY event_date DESC, id DESC").stream().map(RowMapperUtil::camel).toList());
    }

    @GetMapping("/interests")
    public ApiResponse<List<Map<String, Object>>> interests(@RequestParam(required = false) String category) {
        if (category == null || category.isBlank()) return ApiResponse.ok(jdbcTemplate.queryForList("SELECT id, category, title, summary, link FROM personal_interests ORDER BY id DESC").stream().map(RowMapperUtil::camel).toList());
        return ApiResponse.ok(jdbcTemplate.queryForList("SELECT id, category, title, summary, link FROM personal_interests WHERE category = ? ORDER BY id DESC", category).stream().map(RowMapperUtil::camel).toList());
    }

    @GetMapping("/friend-links")
    public ApiResponse<List<Map<String, Object>>> friendLinks() {
        return ApiResponse.ok(jdbcTemplate.queryForList("SELECT id, name, description, url FROM friend_links ORDER BY id").stream().map(RowMapperUtil::camel).toList());
    }

    @PostMapping("/private-content")
    public ApiResponse<Map<String, Object>> privateContent(@AuthenticationPrincipal UserPrincipal principal, @RequestBody PrivateContentRequest request) {
        if (principal == null) throw new IllegalArgumentException("请先登录");
        if (request.password() == null || request.password().isBlank()) throw new IllegalArgumentException("请输入访问密码");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, title, content, password_hash FROM private_contents ORDER BY id LIMIT 1");
        if (rows.isEmpty() || !passwordEncoder.matches(request.password(), String.valueOf(rows.getFirst().get("password_hash")))) throw new IllegalArgumentException("访问密码不正确");
        Map<String, Object> result = new LinkedHashMap<>(); result.put("id", rows.getFirst().get("id")); result.put("title", rows.getFirst().get("title")); result.put("content", rows.getFirst().get("content"));
        return ApiResponse.ok(result);
    }

    public record PrivateContentRequest(String password) {}
}
