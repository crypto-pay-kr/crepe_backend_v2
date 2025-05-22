package dev.crepe.domain.auth.sse.controller;

import dev.crepe.domain.auth.jwt.util.JwtAuthentication;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;
import dev.crepe.domain.auth.sse.service.impl.DuplicateLoginNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class AuthEventController {

    private final DuplicateLoginNotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(value = "/api/auth/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter authEvents(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or missing token");
        }

        JwtAuthentication auth = jwtTokenProvider.getAuthentication(token);
        String userEmail = auth.getUserEmail();

        // 여기서 SSE 연결 등록
        return notificationService.registerUser(userEmail);
    }
}