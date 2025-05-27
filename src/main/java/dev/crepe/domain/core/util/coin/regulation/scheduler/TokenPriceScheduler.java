package dev.crepe.domain.core.util.coin.regulation.scheduler;

import dev.crepe.domain.core.util.coin.regulation.service.TokenPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenPriceScheduler {

    private final TokenPriceService tokenPriceService;

    // 매 1시간마다 실행
    @Scheduled(cron = "0 0 * * * ?")
    public void fetchAndSaveTokenPrice() {
        log.info("토큰 시세 스케줄링 시작");
        tokenPriceService.saveTokenPrice();
    }
}
