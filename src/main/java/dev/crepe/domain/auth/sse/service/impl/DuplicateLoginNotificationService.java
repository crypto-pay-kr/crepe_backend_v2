package dev.crepe.domain.auth.sse.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateLoginNotificationService {

    // 사용자별 SSE 연결 관리
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    /**
     * 사용자의 SSE 연결 등록
     */
    public SseEmitter registerUser(String userEmail) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 기존 연결이 있다면 해제
        SseEmitter existingEmitter = userEmitters.get(userEmail);
        if (existingEmitter != null) {
            try {
                existingEmitter.complete();
            } catch (Exception e) {
                // 무시
            }
        }

        // 새 연결 등록
        userEmitters.put(userEmail, emitter);

        // 연결 해제 시 정리
        emitter.onCompletion(() -> userEmitters.remove(userEmail));
        emitter.onTimeout(() -> userEmitters.remove(userEmail));
        emitter.onError((e) -> userEmitters.remove(userEmail));

        // 연결 성공 메시지
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Authentication monitoring started"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        log.info("SSE registered for user: {}", userEmail);
        return emitter;
    }

    /**
     * 중복 로그인 알림 전송
     */
    public void notifyDuplicateLogin(String userEmail) {
        SseEmitter emitter = userEmitters.get(userEmail);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("duplicate-login")
                        .data("Another device has logged in. You will be logged out.")
                        .id(String.valueOf(System.currentTimeMillis())));

                log.info("Sent duplicate login notification to: {}", userEmail);

                // 잠시 후 연결 해제 (프론트엔드에서 처리할 시간 제공)
                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                        .execute(() -> {
                            try {
                                emitter.complete();
                            } catch (Exception e) {
                                // 무시
                            }
                            userEmitters.remove(userEmail);
                        });

            } catch (IOException e) {
                log.error("Failed to send duplicate login notification to: {}", userEmail, e);
                userEmitters.remove(userEmail);
            }
        } else {
            log.info("No SSE connection found for user: {}", userEmail);
        }
    }

    /**
     * 특정 사용자의 연결 해제
     */
    public void disconnectUser(String userEmail) {
        SseEmitter emitter = userEmitters.remove(userEmail);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                // 무시
            }
        }
    }

    /**
     * 현재 연결된 사용자 수
     */
    public int getConnectedUserCount() {
        return userEmitters.size();
    }
}