package com.myblog.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final String frontendUrl;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, @Value("${app.frontend-url}") String frontendUrl) { this.jwtAuthFilter = jwtAuthFilter; this.frontendUrl = frontendUrl; }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource())).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/reset-password", "/api/auth/reset-password/**", "/api/auth/request-reset-code", "/api/health").permitAll()
                .requestMatchers("/api/articles/**", "/api/categories", "/api/tags", "/api/archive").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/messages", "/api/messages/**", "/api/replies/latest").permitAll()
                .requestMatchers("/api/profile", "/api/timeline", "/api/interests", "/api/friend-links").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reading-data").permitAll()
                .requestMatchers("/api/auth/me", "/api/auth/change-password", "/api/private-content", "/api/drafts/**", "/api/reading-heatmap").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/messages", "/api/messages/**/replies").permitAll()
                .anyRequest().authenticated()).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", configuration); return source;
    }
}