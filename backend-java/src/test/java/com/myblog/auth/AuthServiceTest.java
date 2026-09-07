package com.myblog.auth;

import com.myblog.security.JwtService;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private JdbcTemplate jdbcTemplate;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:auth" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(30) UNIQUE NOT NULL, email VARCHAR(120), phone VARCHAR(30), password_hash VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL, status VARCHAR(20) NOT NULL, avatar_url VARCHAR(500), created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE password_reset_records (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, token_hash VARCHAR(255) NOT NULL, expires_at TIMESTAMP NOT NULL, used_at TIMESTAMP)");
        passwordEncoder = new BCryptPasswordEncoder();
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.createToken(any())).thenReturn("test-token");
        authService = new AuthService(jdbcTemplate, passwordEncoder, jwtService, 15, false);
        jdbcTemplate.update("INSERT INTO users (username, email, phone, password_hash, role, status) VALUES (?, ?, ?, ?, ?, ?)", "alice", "alice@example.com", "13800000000", passwordEncoder.encode("old-password"), "user", "active");
    }

    @Test
    void resetPasswordConsumesValidCodeAndChangesPassword() {
        jdbcTemplate.update("INSERT INTO password_reset_records (user_id, token_hash, expires_at) VALUES (?, ?, DATEADD('MINUTE', 10, CURRENT_TIMESTAMP))", 1L, passwordEncoder.encode("654321"));

        authService.resetPassword(new AuthController.ResetPasswordRequest("alice", null, null, "654321", null, "new-password"));

        String passwordHash = jdbcTemplate.queryForObject("SELECT password_hash FROM users WHERE id = 1", String.class);
        assertThat(passwordEncoder.matches("new-password", passwordHash)).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT used_at IS NOT NULL FROM password_reset_records WHERE id = 1", Boolean.class)).isTrue();
        assertThatThrownBy(() -> authService.resetPassword(new AuthController.ResetPasswordRequest("alice", null, null, "654321", null, "another-password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("验证码无效或已过期");
    }

    @Test
    void expiredCodeCannotResetPassword() {
        jdbcTemplate.update("INSERT INTO password_reset_records (user_id, token_hash, expires_at) VALUES (?, ?, DATEADD('MINUTE', -1, CURRENT_TIMESTAMP))", 1L, passwordEncoder.encode("654321"));

        assertThatThrownBy(() -> authService.resetPassword(new AuthController.ResetPasswordRequest("alice", null, null, "654321", null, "new-password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("验证码无效或已过期");
    }

    @Test
    void requestCodeReplacesOutstandingCodeWithoutExposingItByDefault() {
        jdbcTemplate.update("INSERT INTO password_reset_records (user_id, token_hash, expires_at) VALUES (?, ?, DATEADD('MINUTE', 10, CURRENT_TIMESTAMP))", 1L, passwordEncoder.encode("111111"));

        var result = authService.requestResetCode(new AuthController.ResetCodeRequest(null, "alice", null));

        assertThat(result).containsEntry("expiresInSeconds", 900).doesNotContainKey("code");
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM password_reset_records WHERE user_id = 1 AND used_at IS NULL", Integer.class)).isEqualTo(1);
    }

    @Test
    void verifyCodeAcceptsLegacyTokenAndRejectsMalformedCodes() {
        jdbcTemplate.update("INSERT INTO password_reset_records (user_id, token_hash, expires_at) VALUES (?, ?, DATEADD('MINUTE', 10, CURRENT_TIMESTAMP))", 1L, passwordEncoder.encode("012345"));

        authService.verifyResetCode(new AuthController.VerifyResetCodeRequest(null, "alice", null, null, "012345"));

        assertThatThrownBy(() -> authService.verifyResetCode(new AuthController.VerifyResetCodeRequest("alice", null, null, "123", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("请输入 6 位验证码");
    }

    @Test
    void loginAcceptsLegacyAccountField() {
        var result = authService.login(new AuthController.LoginRequest(null, "alice", "old-password"));

        assertThat(result).containsEntry("token", "test-token");
        assertThat(((java.util.Map<?, ?>) result.get("user")).get("username")).isEqualTo("alice");
    }
}