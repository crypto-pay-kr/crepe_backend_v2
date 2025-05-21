package dev.crepe.domain.core.subscribe.service;

import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.util.history.subscribe.model.dto.SubscribeHistoryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface SubscribeService {

    List<SubscribeResponseDto> getUserSubscribes(String email);
    Slice<SubscribeHistoryDto> getHistoryBySubscribe(Long subscribeId, String email, Pageable pageable);
}
