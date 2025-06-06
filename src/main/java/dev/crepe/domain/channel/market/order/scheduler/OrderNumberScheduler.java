package dev.crepe.domain.channel.market.order.scheduler;
import dev.crepe.domain.channel.market.order.service.impl.OrderNumberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderNumberScheduler {

    private final OrderNumberServiceImpl orderNumberService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetOrderNumbers() {
        orderNumberService.resetOrderNumbersForAllStores();
    }
}