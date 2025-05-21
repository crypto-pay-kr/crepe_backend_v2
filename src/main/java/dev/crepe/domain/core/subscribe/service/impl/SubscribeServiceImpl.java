package dev.crepe.domain.core.subscribe.service.impl;

import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.subscribe.service.SubscribeService;
import dev.crepe.domain.core.util.history.subscribe.model.dto.SubscribeHistoryDto;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final SubscribeHistoryRepository subscribeHistoryRepository;


    // 가입된 상품 조회
    public List<SubscribeResponseDto> getUserSubscribes(String email) {
        return subscribeRepository.findAllByUser_Email(email)
                .stream()
                .map(SubscribeResponseDto::from)
                .toList();
    }

    // 가입된 상품 거래내역 조회
    public Slice<SubscribeHistoryDto> getHistoryBySubscribe(Long subscribeId, String email,Pageable pageable) {
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));

        if (!subscribe.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("해당 상품에 접근할 수 없습니다.");
        }

        Slice<SubscribeHistory> slice = subscribeHistoryRepository.findAllBySubscribe_IdOrderByCreatedAtDesc(subscribeId, pageable);

        return slice.map(SubscribeHistoryDto::from);
    }

}
