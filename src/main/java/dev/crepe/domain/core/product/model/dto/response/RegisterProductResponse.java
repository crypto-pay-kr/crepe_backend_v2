package dev.crepe.domain.core.product.model.dto.response;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.request.EligibilityCriteriaDto;
import dev.crepe.domain.core.product.model.dto.request.PreferentialRateConditionDto;
import dev.crepe.domain.core.product.model.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class RegisterProductResponse {
    private Long productId;
    private String productName;
    private BankProductType type;
}
