package dev.crepe.domain.core.util.history.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PayMonthlyAmountDto {
    private int month;
    private BigDecimal totalAmount;
}
