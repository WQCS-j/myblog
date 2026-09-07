package com.myblog.auth;

import com.myblog.common.RowMapperUtil;
import com.myblog.security.JwtService;
import com.myblog.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final int resetCodeTtlMinutes;
    private final boolean exposeResetCode;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.password-reset.ttl-minutes:15}") int resetCodeTtlMinutes,
            @Value("${app.password-reset.debug-expose-code:false}") boolean exposeResetCode
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.resetCodeTtlMinutes = resetCodeTtlMinutes;
        this.exposeResetCode = exposeResetCode;
    }

    @Transactional
    public Map<String, Object> register(AuthController.RegisterRequest request) {
        String username = request.username().trim();
        String email = blankToNull(request.email());
        if (email != null && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("邮箱格式不正确");
        if (!Boolean.TRUE.equals(request.agreementAccepted())) throw new IllegalArgumentException("请先同意用户协议");
        String phone = blankToNull(request.phone());
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ? OR (? IS NOT NULL AND email = ?) OR (? IS NOT NULL AND phone = ?)", Integer.class, username, email, email, phone, phone);
        if (count != null && count > 0) throw new IllegalArgumentException("用户名、邮箱或手机号已被注册");
        try { jdbcTemplate.update("INSERT INTO users (username, email, phone, password_hash, role, status) VALUES (?, ?, ?, ?, 'user', 'active')", username, email, phone, passwordEncoder.encode(request.password())); }
        catch (DuplicateKeyException exception) { throw new IllegalArgumentException("用户名、邮箱或手机号已被注册"); }
        return Map.of("username", username);
    }

    public Map<String, Object> login(AuthController.LoginRequest request) {
        String account = account(request.identifier(), request.account(), null);
        var users = jdbcTemplate.queryForList("SELECT id, username, password_hash, role, status FROM users WHERE username = ? OR email = ? OR phone = ? LIMIT 1", account, account, account);
        if (users.isEmpty()) throw new IllegalArgumentException("账号或密码错误");
        var user = users.getFirst();
        if (!passwordEncoder.matches(request.password(), String.valueOf(user.get("password_hash")))) throw new IllegalArgumentException("账号或密码错误");
        if (!"active".equalsIgnoreCase(String.valueOf(user.get("status")))) throw new IllegalArgumentException("账号暂不可用");
        var principal = new UserPrincipal(((Number) user.get("id")).longValue(), String.valueOf(user.get("username")), String.valueOf(user.get("role")));
        var result = new HashMap<String, Object>(); result.put("token", jwtService.createToken(principal)); result.put("user", Map.of("id", principal.userId(), "username", principal.username(), "role", principal.role())); return result;
    }

    public Map<String, Object> me(Long userId) {
        var users = jdbcTemplate.queryForList("SELECT id, username, email, phone, role, avatar_url, created_at FROM users WHERE id = ?", userId);
        if (users.isEmpty()) throw new IllegalArgumentException("用户不存在");
        return RowMapperUtil.camel(users.getFirst());
    }

    @Transactional
    public Map<String, Object> requestResetCode(AuthController.ResetCodeRequest request) {
        Long userId = findActiveUserId(account(request.identifier(), request.account(), request.email()));
        if (userId == null) return Map.of("expiresInSeconds", resetCodeTtlMinutes * 60);
        String code = "%06d".formatted(secureRandom.nextInt(1_000_000));
        jdbcTemplate.update("DELETE FROM password_reset_records WHERE user_id = ? AND used_at IS NULL", userId);
        jdbcTemplate.update("INSERT INTO password_reset_records (user_id, token_hash, expires_at) VALUES (?, ?, ?)", userId, passwordEncoder.encode(code), LocalDateTime.now().plusMinutes(resetCodeTtlMinutes));
        Map<String, Object> result = new HashMap<>();
        result.put("expiresInSeconds", resetCodeTtlMinutes * 60);
        if (exposeResetCode) result.put("code", code);
        return result;
    }

    public void verifyResetCode(AuthController.VerifyResetCodeRequest request) {
        Long userId = findActiveUserId(account(request.identifier(), request.account(), request.email()));
        if (userId == null || !hasValidResetCode(userId, verificationCode(request.code(), request.token()))) throw new IllegalArgumentException("验证码无效或已过期");
    }

    @Transactional
    public void resetPassword(AuthController.ResetPasswordRequest request) {
        Long userId = findActiveUserId(account(request.identifier(), request.account(), request.email()));
        if (userId == null) throw new IllegalArgumentException("验证码无效或已过期");
        String code = verificationCode(request.code(), request.token());
        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT id, token_hash FROM password_reset_records WHERE user_id = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP ORDER BY id DESC LIMIT 1", userId);
        if (records.isEmpty() || !passwordEncoder.matches(code, String.valueOf(records.getFirst().get("token_hash")))) throw new IllegalArgumentException("验证码无效或已过期");
        long recordId = ((Number) records.getFirst().get("id")).longValue();
        if (jdbcTemplate.update("UPDATE password_reset_records SET used_at = CURRENT_TIMESTAMP WHERE id = ? AND used_at IS NULL", recordId) != 1) throw new IllegalArgumentException("验证码无效或已过期");
        jdbcTemplate.update("UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", passwordEncoder.encode(request.newPassword()), userId);
    }

    @Transactional
    public void changePassword(Long userId, AuthController.ChangePasswordRequest request) {
        var users = jdbcTemplate.queryForList("SELECT password_hash FROM users WHERE id = ? AND status = 'active'", userId);
        if (users.isEmpty() || !passwordEncoder.matches(request.oldPassword(), String.valueOf(users.getFirst().get("password_hash")))) throw new IllegalArgumentException("原密码不正确");
        jdbcTemplate.update("UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", passwordEncoder.encode(request.newPassword()), userId);
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String account(String identifier, String account, String email) {
        String value = blankToNull(identifier);
        if (value == null) value = blankToNull(account);
        if (value == null) value = blankToNull(email);
        if (value == null) throw new IllegalArgumentException("账号不能为空");
        return value;
    }
    private String verificationCode(String code, String token) {
        String value = blankToNull(code);
        if (value == null) value = blankToNull(token);
        if (value == null || !value.matches("\\d{6}")) throw new IllegalArgumentException("请输入 6 位验证码");
        return value;
    }
    private Long findActiveUserId(String account) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id FROM users WHERE status = 'active' AND (username = ? OR email = ? OR phone = ?) LIMIT 1", account, account, account);
        return users.isEmpty() ? null : ((Number) users.getFirst().get("id")).longValue();
    }
    private boolean hasValidResetCode(long userId, String code) {
        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT token_hash FROM password_reset_records WHERE user_id = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP ORDER BY id DESC LIMIT 1", userId);
        return !records.isEmpty() && passwordEncoder.matches(code, String.valueOf(records.getFirst().get("token_hash")));
    }
}