package com.myblog.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DraftRequestTest {
    @Test
    void acceptsRiskInfoAsTheObjectSentByTheVueClient() throws Exception {
        DraftController.DraftRequest request = new ObjectMapper().readValue("{\"title\":\"Draft\",\"riskInfo\":{\"score\":1}}", DraftController.DraftRequest.class);

        assertThat(request.riskInfo().path("score").asInt()).isEqualTo(1);
    }
}