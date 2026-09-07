package com.myblog.article;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArticleControllerTest {
    private JdbcTemplate jdbcTemplate;
    private ArticleController controller;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        controller = new ArticleController(jdbcTemplate);
    }

    @Test
    void searchRouteUsesTheSameArticleListingHandler() throws NoSuchMethodException {
        Method method = ArticleController.class.getDeclaredMethod("list", String.class, Integer.class, String.class, String.class, int.class, Integer.class, Integer.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertThat(mapping.value()).containsExactlyInAnyOrder("/articles", "/articles/search");
    }

    @Test
    void archiveGroupsMysqlTimestampValues() {
        Timestamp publishedAt = Timestamp.valueOf(LocalDateTime.of(2026, 9, 7, 8, 30));
        when(jdbcTemplate.queryForList("SELECT id, title, published_at FROM articles WHERE status = 'published' ORDER BY published_at DESC"))
                .thenReturn(List.of(Map.of("id", 1L, "title", "Java migration", "published_at", publishedAt)));

        var archive = controller.archive().data();

        assertThat(archive.get("2026").get("09")).singleElement()
                .satisfies(article -> assertThat(article).containsEntry("title", "Java migration"));
    }
}