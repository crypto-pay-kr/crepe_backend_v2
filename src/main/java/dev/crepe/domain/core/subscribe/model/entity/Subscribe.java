package dev.crepe.domain.core.subscribe.model.entity;

import dev.crepe.domain.channel.actor.user.model.entity.User;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "subscribe")
public class Subscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 상품과의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscribeStatus status;

    // 가입일
    @Column(name = "subscribeDate", nullable = false)
    private LocalDateTime subscribeDate;

    // 만기일
    @Column(name = "expiredDate", nullable = false)
    private LocalDateTime expiredDate;

    // 현재 잔액
    @Column(name="balance", precision = 18, scale = 8, nullable = false)
    private BigDecimal balance;

    // 이자율
    @Column(name = "interestRate", nullable = false)
    private float interestRate;

    // 마지막 이자 지급일
    @Column(name = "lastInterestPaidDate")
    private LocalDateTime lastInterestPaidDate;



}
