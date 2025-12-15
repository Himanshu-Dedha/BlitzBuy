package com.example.blitzbuy.config;

import com.example.blitzbuy.service.impl.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final CacheService cacheService;

    // LIMIT: 5 Requests per Minute
    private static final int MAX_REQUESTS = 5;
    private static final int WINDOW_SECONDS = 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        // 1. Identify the User
        // this comes from the JWT Token. However here we will get it from request params
        // For this project, we read the Header "X-User-Id" or Query Param "userId"
        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            // Fallback to check query param if header is missing (for testing ease)
            userId = request.getParameter("userId");
        }

        if (userId == null) {
            // If we can't identify the user, we block them (or allow anonymous with strict limits)
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing User ID");
            return false;
        }

        // 2. Redis Logic (Fixed Window Counter)
        String key = "rate_limit:user:" + userId;
        Long requests = cacheService.incrementCounter(key);

        // If this is the FIRST request (1), set the TTL
        if (requests != null && requests == 1) {
            cacheService.setTTL(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        // 3. Check Threshold
        if (requests != null && requests > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            response.getWriter().write("Too many requests. Slow down!");
            return false; // STOP the request here. Do not go to Controller.
        }
        return true; // Allow request to proceed
    }
}
