package com.myblog.reading;

import com.myblog.common.ApiResponse;
import com.myblog.common.RowMapperUtil;
import com.myblog.security.UserPrincipal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ReadingController {
    private final JdbcTemplate jdbcTemplate;
    public ReadingController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @PostMapping("/reading-data")
    public ApiResponse<Void> record(@RequestBody ReadingRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        if (request.articleId() == null) throw new IllegalArgumentException("文章编号不能为空");
        int duration = Math.min(Math.max(request.durationSeconds() == null ? 0 : request.durationSeconds(), 0), 86400);
        int scroll = Math.min(Math.max(request.scrollDepth() == null ? 0 : request.scrollDepth(), 0), 100);
        jdbcTemplate.update("INSERT INTO reading_data (article_id, user_id, duration_seconds, scroll_depth) VALUES (?, ?, ?, ?)", request.articleId(), principal == null ? null : principal.userId(), duration, scroll);
        if (request.sectionKey() != null && !request.sectionKey().isBlank()) jdbcTemplate.update("INSERT INTO reading_heatmap_records (article_id, section_key, dwell_seconds) VALUES (?, ?, ?)", request.articleId(), request.sectionKey().trim(), Math.min(Math.max(request.dwellSeconds() == null ? 0 : request.dwellSeconds(), 0), 86400));
        return ApiResponse.ok(null, "阅读数据已记录");
    }

    @GetMapping("/reading-heatmap")
    @PreAuthorize("hasAnyRole('author','admin')")
    public ApiResponse<Map<String, Object>> heatmap(@RequestParam int articleId, @AuthenticationPrincipal UserPrincipal principal) {
        if (!"admin".equalsIgnoreCase(principal.role())) {
            Integer owned = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM articles WHERE id = ? AND author_id = ?", Integer.class, articleId, principal.userId());
            if (owned == null || owned == 0) throw new IllegalArgumentException("只能查看自己文章的数据");
        }
        Map<String, Object> summary = jdbcTemplate.queryForMap("SELECT COUNT(DISTINCT COALESCE(user_id, CONCAT('guest-', id))) AS readers, COALESCE(AVG(duration_seconds), 0) AS average_duration, COALESCE(AVG(scroll_depth), 0) AS average_scroll FROM reading_data WHERE article_id = ?", articleId);
        List<Map<String, Object>> sections = jdbcTemplate.queryForList("SELECT section_key, SUM(dwell_seconds) AS dwell_seconds, COUNT(*) AS visits FROM reading_heatmap_records WHERE article_id = ? GROUP BY section_key ORDER BY dwell_seconds DESC", articleId).stream().map(RowMapperUtil::camel).toList();
        Map<String, Object> result = new LinkedHashMap<>(); result.put("summary", RowMapperUtil.camel(summary)); result.put("sections", sections);
        return ApiResponse.ok(result);
    }

    public record ReadingRequest(Integer articleId, Integer durationSeconds, Integer scrollDepth, String sectionKey, Integer dwellSeconds) {}
}
