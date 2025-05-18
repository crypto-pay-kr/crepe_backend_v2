package dev.crepe.domain.core.util.history.token.model.entity;

import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.TokenRequestType;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "token_history")
@Inheritance(strategy = InheritanceType.JOINED)
public class TokenHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토큰의 기본 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id", nullable = false)
    private BankToken bankToken;

    private BigDecimal totalSupplyAmount;
    private String changeReason;
    private String rejectReason;

    @Enumerated(EnumType.STRING)
    private TokenRequestType requestType;

    @Enumerated(EnumType.STRING)
    private BankTokenStatus status;

    @OneToMany(mappedBy = "tokenHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioHistoryDetail> portfolioDetails = new ArrayList<>();

    @Builder
    public TokenHistory(BankToken bankToken, BigDecimal totalSupplyAmount,
                        String changeReason, String rejectReason, TokenRequestType requestType, BankTokenStatus status) {
        this.bankToken = bankToken;
        this.totalSupplyAmount = totalSupplyAmount;
        this.changeReason = changeReason;
        this.rejectReason = rejectReason;
        this.requestType = requestType;
        this.status = status;
    }

    public void updateStatus(BankTokenStatus status ) {
        this.status = status;
    }

    public void addRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}