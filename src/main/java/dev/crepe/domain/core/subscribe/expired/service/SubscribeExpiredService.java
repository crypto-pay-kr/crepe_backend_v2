package dev.crepe.domain.core.subscribe.expired.service;

// 만기 해지
public interface SubscribeExpiredService {
    String expired(String userEmail, Long subscribeId);
}
