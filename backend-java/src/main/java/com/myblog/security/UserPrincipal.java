package com.myblog.security;

public record UserPrincipal(Long userId, String username, String role) {
}
