package dev.crepe.domain.bank.model.dto.response;

import dev.crepe.domain.core.product.model.BankProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetAllProductResponse {
    private Long id;
    private String productName;
    private String type;
    private BankProductStatus status;
    private String description;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private Float baseInterestRate;
    private BigDecimal maxMonthlyPayment;
    private Integer maxParticipants;
    private String imageUrl;
    private String guideFileUrl;
    private List<String> tags;
}