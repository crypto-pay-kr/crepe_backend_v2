package dev.crepe.domain.core.util.history.token.model.entity;

import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED) // 상속 구조를 위한 설정
public abstract class PortfolioHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bank_token_id", nullable = false)
    private BankToken bankToken;

    public PortfolioHistory(BankToken bankToken) {
        this.bankToken = bankToken;
    }
}