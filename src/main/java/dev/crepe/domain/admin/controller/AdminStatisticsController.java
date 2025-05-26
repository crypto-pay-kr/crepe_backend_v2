package dev.crepe.domain.admin.controller;

import dev.crepe.domain.auth.role.AdminAuth;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.domain.core.util.history.business.model.dto.CoinUsageDto;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.business.service.impl.TransactionHistoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    @Operation(summary = "actor 역할 수 조회", description = "actor 역할 수를 조회합니다.")
    @GetMapping("/role/count")
    @AdminAuth
    public Map<String, Long> getActorRoleCounts() {
        return actorService.getRoleCounts();
    }

    // 총 코인 거래량
    @GetMapping("/coin/total")
    @AdminAuth
    public Map<String, BigDecimal> getUserCoinTransactionTotal() {
        return Map.of("userCoinTotal", transactionHistoryService.getUserCoinTransactionTotal());
    }

    // currency 코인 거래량
    @Operation(summary = "currency 코인 거래량 조회", description = "currency별 코인 거래량을 조회합니다.")
    @GetMapping(value = "/coin/usage")
    @AdminAuth
    public List<CoinUsageDto> getCoinUsageByCurrency() {
        return transactionHistoryService.getCoinUsageForUsers();
    }

}
