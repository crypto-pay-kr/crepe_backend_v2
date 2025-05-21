package dev.crepe.domain.core.subscribe.service.impl;

import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.subscribe.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {

    private final SubscribeRepository subscribeRepository;

    public List<SubscribeResponseDto> getUserSubscribes(String email) {
        return subscribeRepository.findAllByUser_Email(email)
                .stream()
                .map(SubscribeResponseDto::from)
                .toList();
    }
}
