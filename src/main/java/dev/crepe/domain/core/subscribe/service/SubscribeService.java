package dev.crepe.domain.core.subscribe.service;

import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;

import java.util.List;

public interface SubscribeService {

    List<SubscribeResponseDto> getUserSubscribes(String email);
}
