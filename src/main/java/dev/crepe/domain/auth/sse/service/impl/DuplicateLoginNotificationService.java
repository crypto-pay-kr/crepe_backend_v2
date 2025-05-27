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
        // 30분 타임아웃 설정 (Long.MAX_VALUE 대신 실제 시간 사용)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분

        // 새 연결 등록
        userEmitters.put(userEmail, emitter);

        // 연결 해제 시 정리
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료됨: {}", userEmail);
            userEmitters.remove(userEmail);
        });

        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: {}", userEmail);
            userEmitters.remove(userEmail);
        });

        emitter.onError((e) -> {
            log.error("SSE 연결 오류: {}", userEmail, e);
            userEmitters.remove(userEmail);
        });

        // 연결 성공 메시지 전송 - 필수!
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Authentication monitoring started"));

            log.info("SSE 연결 성공 메시지 전송 완료: {}", userEmail);
        } catch (IOException e) {
            log.error("SSE 연결 성공 메시지 전송 실패: {}", userEmail, e);
            userEmitters.remove(userEmail);
            emitter.completeWithError(e);
            return emitter; // 실패 시 바로 리턴
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

                // 3초 후 연결 해제 (프론트엔드에서 처리할 시간 제공)
                CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS)
                        .execute(() -> {
                            try {
                                emitter.complete();
                                log.info("SSE 연결 해제 완료: {}", userEmail);
                            } catch (Exception e) {
                                log.warn("SSE 연결 해제 중 오류: {}", userEmail, e);
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
                log.info("사용자 SSE 연결 해제: {}", userEmail);
            } catch (Exception e) {
                log.warn("사용자 SSE 연결 해제 중 오류: {}", userEmail, e);
            }
        }
    }

    /**
     * 현재 연결된 사용자 수
     */
    public int getConnectedUserCount() {
        return userEmitters.size();
    }

    /**
     * Keep-alive 메시지 전송 (선택적)
     */
    public void sendKeepAlive(String userEmail) {
        SseEmitter emitter = userEmitters.get(userEmail);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("keepalive")
                        .data("ping"));
            } catch (IOException e) {
                log.error("Keep-alive 전송 실패: {}", userEmail, e);
                userEmitters.remove(userEmail);
            }
        }
    }
}