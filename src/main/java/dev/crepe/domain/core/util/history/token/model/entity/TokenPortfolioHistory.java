package dev.crepe.domain.core.util.history.token.model.entity;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED) // 상속 구조를 위한 설정
public class TokenPortfolioHistory extends PortfolioHistory {

    private BigDecimal amount;
    private String changeReason;
    private String description;

    @Builder
    public TokenPortfolioHistory(BankToken bankToken, BigDecimal amount, String description, String changeReason) {
        super(bankToken); // 부모 클래스의 필드 초기화
        this.amount = amount;
        this.description = description;
        this.changeReason = changeReason;
    }

}