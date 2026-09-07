package com.myblog.reading;

import com.myblog.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadingControllerTest {
    private JdbcTemplate jdbcTemplate;
    private ReadingController controller;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        controller = new ReadingController(jdbcTemplate);
    }

    @Test
    void recordUsesCreatedStatusAndRejectsInvalidArticleId() {
        var response = controller.record(new ReadingController.ReadingRequest(9, 20, 60, null, null), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThatThrownBy(() -> controller.record(new ReadingController.ReadingRequest(0, 20, 60, null, null), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("文章编号无效");
    }

    @Test
    void heatmapUsesLegacyAggregateSemantics() {
        when(jdbcTemplate.queryForMap(any(String.class), any(Object[].class)))
                .thenReturn(Map.of("readers", 2L, "average_duration", 30, "average_scroll", 75));
        when(jdbcTemplate.queryForList(any(String.class), any(Object[].class))).thenReturn(java.util.List.of());

        var response = controller.heatmap(9, new UserPrincipal(7L, "author", "author"));

        assertThat(response.data().get("summary")).isEqualTo(Map.of("readers", 2L, "averageDuration", 30, "averageScroll", 75));
        verify(jdbcTemplate).queryForMap(contains("COUNT(*) AS readers"), any(Object[].class));
    }

    @Test
    void heatmapRejectsMissingArticleId() {
        assertThatThrownBy(() -> controller.heatmap(0, new UserPrincipal(7L, "author", "author")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("请选择文章");
    }
}
