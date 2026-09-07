package com.myblog.auth;

import com.myblog.common.ApiResponse;
import com.myblog.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) { return ApiResponse.ok(authService.register(request), "注册成功"); }
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) { return ApiResponse.ok(authService.login(request), "登录成功"); }
    @PostMapping("/logout")
    public ApiResponse<Void> logout() { return ApiResponse.ok(null, "退出成功"); }
    @PostMapping({"/reset-password/code", "/request-reset-code"})
    public ApiResponse<Map<String, Object>> requestResetCode(@RequestBody ResetCodeRequest request) { return ApiResponse.ok(authService.requestResetCode(request), "验证码已发送"); }
    @PostMapping("/reset-password/verify-code")
    public ApiResponse<Void> verifyResetCode(@RequestBody VerifyResetCodeRequest request) { authService.verifyResetCode(request); return ApiResponse.ok(null, "验证码有效"); }
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) { authService.resetPassword(request); return ApiResponse.ok(null, "密码修改成功，请重新登录"); }
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ChangePasswordRequest request) { authService.changePassword(principal.userId(), request); return ApiResponse.ok(null, "密码已修改，请重新登录"); }
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(@AuthenticationPrincipal UserPrincipal principal) { return ApiResponse.ok(authService.me(principal.userId())); }

    public record RegisterRequest(@NotBlank(message = "用户名不能为空") @Size(min = 3, max = 30, message = "用户名长度应为 3 至 30 个字符") String username, String email, String phone, @NotBlank(message = "密码不能为空") @Size(min = 8, max = 72, message = "密码长度应为 8 至 72 个字符") String password, Boolean agreementAccepted) {}
    public record LoginRequest(String identifier, String account, @NotBlank(message = "密码不能为空") String password) {}
    public record ResetCodeRequest(String identifier, String account, String email) {}
    public record VerifyResetCodeRequest(String identifier, String account, String email, String code, String token) {}
    public record ResetPasswordRequest(String identifier, String account, String email, String code, String token, @NotBlank(message = "新密码不能为空") @Size(min = 8, max = 72, message = "密码长度应为 8 至 72 个字符") String newPassword) {}
    public record ChangePasswordRequest(@NotBlank(message = "原密码不能为空") String oldPassword, @NotBlank(message = "新密码不能为空") @Size(min = 8, max = 72, message = "密码长度应为 8 至 72 个字符") String newPassword) {}
}