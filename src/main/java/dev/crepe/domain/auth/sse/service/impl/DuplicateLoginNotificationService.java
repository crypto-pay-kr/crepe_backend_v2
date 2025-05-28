package dev.crepe.domain.auth.sse.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
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
        // 기존 연결이 있다면 해제
        SseEmitter existingEmitter = userEmitters.get(userEmail);
        if (existingEmitter != null) {
            try {
                existingEmitter.complete();
                log.info("기존 SSE 연결 해제: {}", userEmail);
            } catch (Exception e) {
                log.warn("기존 SSE 연결 해제 중 오류: {}", userEmail, e);
            }
            userEmitters.remove(userEmail);
        }

        // 30분 타임아웃 설정
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
                    .data("Authentication monitoring started")
                    .id(String.valueOf(System.currentTimeMillis())));

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
            log.debug("No SSE connection found for user: {} (duplicate login notification)", userEmail);
        }
    }

    /**
     * 토큰 만료 임박 알림 (예: 5분 전)
     */
    public void notifyTokenExpiringSoon(String userEmail, long remainingMinutes) {
        SseEmitter emitter = userEmitters.get(userEmail);
        if (emitter != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("message", String.format("Your session will expire in %d minutes. Please refresh your session.", remainingMinutes));
                data.put("remainingMinutes", remainingMinutes);
                data.put("action", "REFRESH_REQUIRED");
                data.put("timestamp", System.currentTimeMillis());
                data.put("severity", "warning");

                emitter.send(SseEmitter.event()
                        .name("TOKEN_EXPIRING_SOON")
                        .data(data)
                        .id(String.valueOf(System.currentTimeMillis())));

                log.info("⚠️ Token expiring soon notification sent to: {} ({}min remaining)", userEmail, remainingMinutes);
            } catch (IOException e) {
                log.error("❌ Failed to send token expiring notification to: {}", userEmail, e);
                userEmitters.remove(userEmail);
            }
        } else {
            log.debug("No SSE connection found for user: {} (token expiring notification)", userEmail);
        }
    }

    /**
     * 토큰 만료 알림
     */
    public void notifyTokenExpired(String userEmail) {
        SseEmitter emitter = userEmitters.get(userEmail);
        if (emitter != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("message", "Your session has expired. Please login again.");
                data.put("action", "LOGIN_REQUIRED");
                data.put("timestamp", System.currentTimeMillis());
                data.put("severity", "error");

                emitter.send(SseEmitter.event()
                        .name("TOKEN_EXPIRED")
                        .data(data)
                        .id(String.valueOf(System.currentTimeMillis())));

                log.info("❌ Token expired notification sent to: {}", userEmail);

                // 토큰이 만료되면 3초 후 SSE 연결도 종료
                CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS)
                        .execute(() -> {
                            try {
                                emitter.complete();
                                log.info("SSE 연결 해제 완료 (토큰 만료): {}", userEmail);
                            } catch (Exception e) {
                                log.warn("SSE 연결 해제 중 오류 (토큰 만료): {}", userEmail, e);
                            }
                            userEmitters.remove(userEmail);
                        });

            } catch (IOException e) {
                log.error("❌ Failed to send token expired notification to: {}", userEmail, e);
                userEmitters.remove(userEmail);
            }
        } else {
            log.debug("No SSE connection found for user: {} (token expired notification)", userEmail);
        }
    }

    /**
     * 토큰 갱신 성공 알림
     */
    public void notifyTokenRefreshed(String userEmail) {
        SseEmitter emitter = userEmitters.get(userEmail);
        if (emitter != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("message", "Session refreshed successfully.");
                data.put("action", "TOKEN_REFRESHED");
                data.put("timestamp", System.currentTimeMillis());
                data.put("severity", "success");

                emitter.send(SseEmitter.event()
                        .name("TOKEN_REFRESHED")
                        .data(data)
                        .id(String.valueOf(System.currentTimeMillis())));

                log.info("✅ Token refresh notification sent to: {}", userEmail);
            } catch (IOException e) {
                log.error("❌ Failed to send token refresh notification to: {}", userEmail, e);
                userEmitters.remove(userEmail);
            }
        } else {
            log.debug("No SSE connection found for user: {} (token refresh notification)", userEmail);
        }
    }

    /**
     * 시스템 공지사항 브로드캐스트
     */
    public void broadcastSystemNotice(String message, String type) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("type", type);
        data.put("timestamp", System.currentTimeMillis());

        userEmitters.entrySet().removeIf(entry -> {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("SYSTEM_NOTICE")
                        .data(data)
                        .id(String.valueOf(System.currentTimeMillis())));
                return false; // 성공하면 제거하지 않음
            } catch (IOException e) {
                log.error("❌ Failed to send system notice to user: {}", entry.getKey(), e);
                try {
                    entry.getValue().complete();
                } catch (Exception ex) {
                    // 이미 닫힌 연결일 수 있으므로 무시
                }
                return true; // 실패하면 제거
            }
        });

        log.info("📢 System notice broadcasted to {} users: {}", userEmitters.size(), message);
    }

    /**
     * 특정 사용자의 연결 해제
     */
    public void disconnectUser(String userEmail) {
        SseEmitter emitter = userEmitters.remove(userEmail);
        if (emitter != null) {
            try {
                // 연결 해제 알림 전송
                Map<String, Object> data = new HashMap<>();
                data.put("message", "Connection will be closed.");
                data.put("action", "DISCONNECT");
                data.put("timestamp", System.currentTimeMillis());

                emitter.send(SseEmitter.event()
                        .name("DISCONNECT")
                        .data(data)
                        .id(String.valueOf(System.currentTimeMillis())));

                // 1초 후 연결 해제
                CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)
                        .execute(() -> {
                            try {
                                emitter.complete();
                                log.info("🔌 사용자 SSE 연결 해제: {}", userEmail);
                            } catch (Exception e) {
                                log.warn("사용자 SSE 연결 해제 중 오류: {}", userEmail, e);
                            }
                        });

            } catch (IOException e) {
                log.warn("연결 해제 알림 전송 실패: {}", userEmail, e);
                try {
                    emitter.complete();
                } catch (Exception ex) {
                    // 무시
                }
            }
        } else {
            log.debug("No SSE connection found for user: {} (disconnect request)", userEmail);
        }
    }

    /**
     * 현재 연결된 사용자 수
     */
    public int getActiveConnectionCount() {
        return userEmitters.size();
    }

    /**
     * 현재 연결된 사용자 수 (호환성)
     */
    public int getConnectedUserCount() {
        return getActiveConnectionCount();
    }

    /**
     * Keep-alive 메시지 전송 (모든 연결에)
     */
    @Scheduled(fixedRate = 30000) // 30초마다
    public void sendKeepAliveToAll() {
        if (userEmitters.isEmpty()) {
            return;
        }

        userEmitters.entrySet().removeIf(entry -> {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("keepalive")
                        .data("ping")
                        .id(String.valueOf(System.currentTimeMillis())));
                return false; // 성공하면 제거하지 않음
            } catch (IOException e) {
                log.debug("Keep-alive failed for user: {}, removing connection", entry.getKey());
                try {
                    entry.getValue().complete();
                } catch (Exception ex) {
                    // 이미 닫힌 연결일 수 있으므로 무시
                }
                return true; // 실패하면 제거
            }
        });

        log.debug("💓 Keep-alive sent to {} active connections", userEmitters.size());
    }

    /**
     * 특정 사용자에게 Keep-alive 메시지 전송
     */
    public void sendKeepAlive(String userEmail) {
        SseEmitter emitter = userEmitters.get(userEmail);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("keepalive")
                        .data("ping")
                        .id(String.valueOf(System.currentTimeMillis())));
            } catch (IOException e) {
                log.error("Keep-alive 전송 실패: {}", userEmail, e);
                userEmitters.remove(userEmail);
            }
        }
    }

    /**
     * 사용자 연결 상태 확인
     */
    public boolean isUserConnected(String userEmail) {
        return userEmitters.containsKey(userEmail);
    }

    /**
     * 모든 연결된 사용자 목록 (디버깅용)
     */
    public Map<String, String> getConnectedUsers() {
        Map<String, String> connectedUsers = new HashMap<>();
        userEmitters.keySet().forEach(email -> {
            connectedUsers.put(email, "connected");
        });
        return connectedUsers;
    }

    /**
     * 연결 상태 통계
     */
    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", userEmitters.size());
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("connectedUsers", userEmitters.keySet());
        return stats;
    }
}