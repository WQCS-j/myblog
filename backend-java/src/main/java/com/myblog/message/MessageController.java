package com.myblog.message;

import com.myblog.common.ApiResponse;
import com.myblog.common.RowMapperUtil;
import com.myblog.security.UserPrincipal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class MessageController {
    private final JdbcTemplate jdbcTemplate;
    public MessageController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping("/messages")
    public ApiResponse<List<Map<String, Object>>> messages() {
        return ApiResponse.ok(jdbcTemplate.queryForList("SELECT m.id, m.content, m.user_id, COALESCE(u.username, '访客') AS author, m.created_at FROM messages m LEFT JOIN users u ON u.id = m.user_id ORDER BY m.created_at DESC").stream().map(RowMapperUtil::camel).toList());
    }

    @PostMapping("/messages")
    public ApiResponse<Map<String, Object>> publish(@RequestBody MessageRequest request, Authentication authentication) {
        validate(request.content());
        Long userId = userId(authentication);
        jdbcTemplate.update("INSERT INTO messages (user_id, content) VALUES (?, ?)", userId, request.content().trim());
        return ApiResponse.ok(Map.of("content", request.content().trim()), "留言已发布");
    }

    @GetMapping("/messages/{id}/replies")
    public ApiResponse<List<Map<String, Object>>> replies(@PathVariable long id) {
        return ApiResponse.ok(jdbcTemplate.queryForList("SELECT r.id, r.message_id, r.content, r.user_id, COALESCE(u.username, '访客') AS author, r.created_at FROM replies r LEFT JOIN users u ON u.id = r.user_id WHERE r.message_id = ? ORDER BY r.created_at ASC", id).stream().map(RowMapperUtil::camel).toList());
    }

    @PostMapping("/messages/{id}/replies")
    public ApiResponse<Void> reply(@PathVariable long id, @RequestBody MessageRequest request, Authentication authentication) {
        validate(request.content());
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM messages WHERE id = ?", Integer.class, id);
        if (count == null || count == 0) throw new IllegalArgumentException("留言不存在");
        jdbcTemplate.update("INSERT INTO replies (message_id, user_id, content) VALUES (?, ?, ?)", id, userId(authentication), request.content().trim());
        return ApiResponse.ok(null, "回复已发布");
    }

    @GetMapping("/replies/latest")
    public ApiResponse<List<Map<String, Object>>> latestReplies() {
        return ApiResponse.ok(jdbcTemplate.queryForList("SELECT r.id, r.content, r.created_at, COALESCE(u.username, '访客') AS author, r.message_id FROM replies r LEFT JOIN users u ON u.id = r.user_id ORDER BY r.created_at DESC LIMIT 10").stream().map(RowMapperUtil::camel).toList());
    }

    private void validate(String content) { if (content == null || content.isBlank()) throw new IllegalArgumentException("内容不能为空"); if (content.trim().length() > 200) throw new IllegalArgumentException("内容不能超过 200 个字"); }
    private Long userId(Authentication authentication) { if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) return principal.userId(); return null; }
    public record MessageRequest(String content) {}
}
