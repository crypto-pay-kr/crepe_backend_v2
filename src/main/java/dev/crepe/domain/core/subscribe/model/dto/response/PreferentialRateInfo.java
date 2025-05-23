package dev.crepe.domain.core.subscribe.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreferentialRateInfo {
    private BigDecimal rate;
    private String description;
    private String title;
    private String status;
}
