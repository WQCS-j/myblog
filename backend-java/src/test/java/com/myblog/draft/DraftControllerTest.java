package com.myblog.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DraftControllerTest {
    private JdbcTemplate jdbcTemplate;
    private DraftController controller;
    private UserPrincipal author;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        controller = new DraftController(jdbcTemplate, new ObjectMapper());
        author = new UserPrincipal(7L, "author", "author");
    }

    @Test
    void updateReturns404ForMissingOrNonDraftOwnedByAuthor() {
        when(jdbcTemplate.update(any(String.class), any(Object[].class))).thenReturn(0);

        assertThatThrownBy(() -> controller.update(author, 12L, new DraftController.DraftRequest("Title", "", "Body", null, null)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("草稿不存在或无权修改");
    }

    @Test
    void listSupportsLegacyCreatedAtOrdering() {
        when(jdbcTemplate.queryForList(any(String.class), any(Object[].class))).thenReturn(List.of());

        controller.list(author, "", "createdAt");

        verify(jdbcTemplate).queryForList(contains("ORDER BY created_at DESC"), any(Object[].class));
    }

    @Test
    void publishReturnsCreatedWithArticleIdAndSlug() {
        when(jdbcTemplate.queryForList(any(String.class), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", 12L, "title", "Java", "summary", "Summary", "content", "Body", "category_id", 3, "status", "draft", "risk_info", "{}")));
        when(jdbcTemplate.update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 99L));
                    return 1;
                });
        when(jdbcTemplate.update(any(String.class), any(Object[].class))).thenReturn(1);

        var response = controller.publish(author, 12L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).containsKeys("articleId", "slug").containsEntry("articleId", 99L);
    }

    @Test
    void publishReturns400ForInvalidTitleOrContent() {
        when(jdbcTemplate.queryForList(any(String.class), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", 12L, "title", "", "summary", "", "content", "", "category_id", 3, "status", "draft", "risk_info", "{}")));

        assertThatThrownBy(() -> controller.publish(author, 12L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("标题至少需要 2 个字");
    }
}
