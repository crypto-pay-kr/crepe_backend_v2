package dev.crepe.domain.core.util.history.subscribe.model.dto;

import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
public class SubscribeHistoryDto {
    private String eventType;
    private BigDecimal amount;
    private String date;

    public static SubscribeHistoryDto from(SubscribeHistory entity) {
        return SubscribeHistoryDto.builder()
                .eventType(entity.getEventType().name())
                .amount(entity.getAmount())
                .date(entity.getCreatedAt().toString())
                .build();
    }
}