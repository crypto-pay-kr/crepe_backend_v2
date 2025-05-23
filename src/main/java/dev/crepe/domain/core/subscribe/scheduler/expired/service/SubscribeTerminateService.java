package dev.crepe.domain.core.subscribe.scheduler.expired.service;

import dev.crepe.domain.core.subscribe.model.dto.response.TerminatePreviewDto;

// 중도 해지
public interface SubscribeTerminateService {
    String terminate(String userEmail, Long subscribeId);
    TerminatePreviewDto TerminationPreview(String userEmail, Long subscribeId);
}
