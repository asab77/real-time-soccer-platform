package com.example.soccerplatform.config;

import com.example.soccerplatform.exception.InternalApiKeyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiKeyInterceptor implements HandlerInterceptor {

    static final String HEADER_NAME = "X-Internal-Api-Key";

    private final String expectedApiKey;

    public InternalApiKeyInterceptor(
            @Value("${app.internal-sync-api-key:}") String expectedApiKey
    ) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String suppliedApiKey = request.getHeader(HEADER_NAME);

        if (expectedApiKey.isBlank()
                || suppliedApiKey == null
                || suppliedApiKey.isBlank()
                || !keysMatch(expectedApiKey, suppliedApiKey)) {
            throw new InternalApiKeyException();
        }

        return true;
    }

    private boolean keysMatch(String expected, String supplied) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8)
        );
    }
}
