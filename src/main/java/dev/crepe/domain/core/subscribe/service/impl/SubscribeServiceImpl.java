package dev.crepe.domain.core.subscribe.service.impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeVoucherDto;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.subscribe.service.SubscribeService;
import dev.crepe.domain.core.util.history.subscribe.model.dto.SubscribeHistoryDto;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final SubscribeHistoryRepository subscribeHistoryRepository;
    private final ActorRepository actorRepository;
    private final ExceptionDbService exceptionDbService;


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
                .orElseThrow(() -> exceptionDbService.getException("SUBSCRIBE_004"));

        if (!subscribe.getUser().getEmail().equals(email)) {
            throw exceptionDbService.getException("SUBSCRIBE_005");
        }

        Slice<SubscribeHistory> slice = subscribeHistoryRepository.findAllBySubscribe_IdOrderByCreatedAtDesc(subscribeId, pageable);

        return slice.map(SubscribeHistoryDto::from);
    }

    // 가입한 상품권 조회
    public List<SubscribeVoucherDto> getAvailableVouchers(String email) {
        Actor user = actorRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        List<Subscribe> vouchers = subscribeRepository.findByUserAndProduct_TypeAndStatus(
                user, BankProductType.VOUCHER, SubscribeStatus.ACTIVE
        );

        return vouchers.stream()
                .map(SubscribeVoucherDto::from)
                .toList();
    }


}
