package dev.crepe.domain.core.subscribe.expired;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscribeExpiredScheduler {

    private final SubscribeRepository subscribeRepository;

    @Scheduled(cron = "0 1 0 * * *") // 매일 00시 01분
    @Transactional
    public void expireVouchersAutomatically() {

        List<Subscribe> vouchersToExpire = subscribeRepository
                .findAllByProduct_TypeAndExpiredDateBeforeAndStatus(
                        BankProductType.VOUCHER,
                        LocalDateTime.now(),
                        SubscribeStatus.ACTIVE
                );

        for (Subscribe voucher : vouchersToExpire) {
            voucher.isExpired();

        }

        subscribeRepository.saveAll(vouchersToExpire);

    }
}

