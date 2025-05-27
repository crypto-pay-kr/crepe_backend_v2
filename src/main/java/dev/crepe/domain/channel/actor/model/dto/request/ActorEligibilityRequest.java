package dev.crepe.domain.channel.actor.model.dto.request;


import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.IncomeLevel;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorEligibilityRequest {
    private AgeGroup ageGroup;
    private Occupation occupation;
    private IncomeLevel incomeLevel;
}
