package dev.crepe.domain.core.product.model.dto.request;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.entity.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class RegisterProductRequest {
   private String productName;
   private BankProductType type;
   private EligibilityCriteriaDto eligibilityCriteria;
   private BigDecimal budget;
   private BigDecimal baseRate;
   private BigDecimal maxMonthlyPayment;
   private Integer maxParticipants;
   private PreferentialRateConditionDto preferentialRateCondition;
   private LocalDateTime startDate;
   private LocalDateTime endDate;
   private List<Tag> tags;
   private String job;
   private String birth;
   private String description;
   private String imageUrl;
}
