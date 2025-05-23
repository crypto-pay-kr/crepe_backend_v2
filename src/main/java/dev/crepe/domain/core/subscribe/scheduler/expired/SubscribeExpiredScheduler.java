package dev.crepe.domain.core.subscribe.scheduler.expired;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.subscribe.scheduler.expired.service.SubscribeExpiredService;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscribeExpiredScheduler {

    private final SubscribeRepository subscribeRepository;
    private final SubscribeExpiredService subscribeExpiredService;

    @Scheduled(cron = "0 1 0 * * *")  // 매일 00시 01분
    @Transactional
    public void expireSubscriptionsAutomatically() {
        LocalDateTime now = LocalDateTime.now();

        // 자동 해지 대상 상품 타입들
        List<BankProductType> autoExpireTypes = List.of(
                BankProductType.VOUCHER,
                BankProductType.SAVING,
                BankProductType.INSTALLMENT
        );

        List<Subscribe> toExpire = new ArrayList<>();

        for (BankProductType type : autoExpireTypes) {
            List<Subscribe> expiredList = subscribeRepository
                    .findAllByProduct_TypeAndExpiredDateBeforeAndStatus(
                            type,
                            now,
                            SubscribeStatus.ACTIVE
                    );
            toExpire.addAll(expiredList);
        }

        for (Subscribe subscribe : toExpire) {
            try {
                log.info("만기 처리 시도: [ID={}, Email={}]", subscribe.getId(), subscribe.getUser().getEmail());
                subscribeExpiredService.expired(subscribe.getUser().getEmail(), subscribe.getId());
            } catch (Exception e) {
                log.error("만기 처리 실패: [ID={}] - {}", subscribe.getId(), e.getMessage(), e);
            }
        }

    }
}

