package com.electronicstore.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private record Limit(int maxRequests, long windowMillis) {}

    private static final Map<String, Limit> LIMITS = Map.of(
            "/api/auth/login", new Limit(5, 15 * 60 * 1000L),
            "/api/auth/register", new Limit(5, 15 * 60 * 1000L),
            "/api/auth/forgot-password", new Limit(3, 60 * 60 * 1000L)
    );

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        Limit limit = LIMITS.get(request.getRequestURI());

        if (limit != null && "POST".equalsIgnoreCase(request.getMethod())) {
            String key = request.getRequestURI() + ":" + clientIp(request);
            long now = System.currentTimeMillis();
            long windowStart = now - limit.windowMillis();

            Deque<Long> timestamps = requestLog.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                    timestamps.pollFirst();
                }
                if (timestamps.size() >= limit.maxRequests()) {
                    respondTooManyRequests(response, request.getRequestURI());
                    return;
                }
                timestamps.addLast(now);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void respondTooManyRequests(HttpServletResponse response, String path) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\"," +
                        "\"message\":\"Too many attempts. Please wait before trying again.\",\"path\":\"%s\"}",
                LocalDateTime.now(), path));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}