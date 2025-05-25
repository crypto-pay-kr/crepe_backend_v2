package dev.crepe.domain.admin.controller;

import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.domain.core.util.history.business.model.dto.CoinUsageDto;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.business.service.impl.TransactionHistoryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final ActorService actorService;
    private final TransactionHistoryService transactionHistoryService;

    // 역할 별 수 count
    @GetMapping("/role/count")
    public Map<String, Long> getActorRoleCounts() {
        return actorService.getRoleCounts();
    }

    // 총 코인 거래량
    @GetMapping("/coin/total")
    public Map<String, BigDecimal> getUserCoinTransactionTotal() {
        return Map.of("userCoinTotal", transactionHistoryService.getUserCoinTransactionTotal());
    }

    @GetMapping("/coin/usage")
    public List<CoinUsageDto> getCoinUsageByCurrency() {
        return transactionHistoryService.getCoinUsageForUsers();
    }

}
