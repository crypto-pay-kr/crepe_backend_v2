package dev.crepe.domain.core.subscribe.expired.service;

// 중도 해지
public interface SubscribeTerminateService {
    String terminate(String userEmail, Long subscribeId);
}
