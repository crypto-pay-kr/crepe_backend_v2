package dev.crepe.domain.core.subscribe.expired.service;

import dev.crepe.domain.core.subscribe.model.dto.response.TerminatePreviewDto;

// 중도 해지
public interface SubscribeTerminateService {
    String terminate(String userEmail, Long subscribeId);
    TerminatePreviewDto calculateTerminationPreview(String userEmail, Long subscribeId);
}
