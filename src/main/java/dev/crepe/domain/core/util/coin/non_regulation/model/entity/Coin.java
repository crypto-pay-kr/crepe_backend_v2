package dev.crepe.domain.core.util.coin.non_regulation.model.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coin")
public class Coin  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String currency;

    @Column(name = "network_type", nullable = false)
    private String networkType;

    @Column(name = "is_tag", nullable = false)
    private boolean isTag;

    @Column(name ="min_deposit", nullable= false)
    private String minDeposit;

    @Column(name="coin_image", nullable = false)
    private String coinImage;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @Column(name="data_status")
    private boolean dataStatus = true;
}