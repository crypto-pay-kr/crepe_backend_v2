package dev.crepe.domain.channel.actor.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.ActorStatus;
import dev.crepe.domain.channel.actor.model.ActorSuspension;
import dev.crepe.domain.channel.actor.model.Gender;
import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import dev.crepe.domain.channel.actor.store.model.StoreType;
import dev.crepe.domain.channel.market.like.model.entity.Like;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.infra.naver.ocr.id.entity.dto.IdCardOcrResponse;
import dev.crepe.infra.naver.ocr.id.util.PersonalNumberParser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.global.base.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="actor")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Actor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(name="nick_name")
    private String nickName;

    @Column(name="phone_number", nullable = false)
    private String phoneNum;

    @Column
    private String birth;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column
    private Occupation occupation;
    // 연간 소득 (null 허용)
    @Column(name="annual_income", precision = 20, scale = 2)
    private BigDecimal annualIncome;

    // 총 자산 (null 허용)
    @Column(name="total_asset", precision = 20, scale = 2)
    private BigDecimal totalAsset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name="store_type")
    private StoreType storeType;

    @Column(name="store_address")
    private String storeAddress;

    @Column(name="business_number")
    private String businessNumber;

    @Column(name="business_image")
    private String businessImage;

    @Column(name="store_image")
    private String storeImage;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="actor_status")
    private ActorStatus actorStatus = ActorStatus.ACTIVE;

    @Embedded
    private ActorSuspension suspension;

    @ManyToMany
    @JoinTable(
            name = "store_coin",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "coin_id")
    )
    @Builder.Default
    private List<Coin> coinList = new ArrayList<>();

    @Builder.Default
    @Column(name="store_status")
    @Enumerated(EnumType.STRING)
    private StoreStatus status = StoreStatus.CLOSED;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @Column(name="store_menus")
    @JsonIgnore
    private List<Menu> menus;

    @Column(name="likes")
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<Like> likes;

    public void changePassword(String newPassword) {this.password = newPassword;}
    public void changePhone(String successNewPhone) {
        this.phoneNum = successNewPhone;
    }
    public void changeName(String newName) {
        this.name = newName;
    }

    public void changeNickname(String newNickname) {this.nickName = newNickname;}
    public void changeStoreAddress(String newStoreAddress) {this.storeAddress = newStoreAddress;}

    public void changeStoreImage(String newStoreImage) {this.storeImage = newStoreImage;}

    public void changeBusiness(String newBusinessNum, String newBusinessImage) {
        this.businessNumber = newBusinessNum;
        this.businessImage = newBusinessImage;}

    public void changeSupportedCoins(List<Coin> supportedCoins) {
        this.coinList = supportedCoins;
    }

    public void changeStoreStatus(StoreStatus newStoreStatus) {
        this.status = newStoreStatus;
    }

    public void updateFinancialInfo(BigDecimal annualIncome, BigDecimal totalAsset) {
        this.annualIncome = annualIncome;
        this.totalAsset = totalAsset;
    }

    public void addOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public void addAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }
    public void addTotalAsset(BigDecimal totalAsset) {
        this.totalAsset = totalAsset;
    }


    public void updateFromIdCard(IdCardOcrResponse idCardResponse) {
        // 이름 업데이트
        if (idCardResponse.getName() != null && !idCardResponse.getName().trim().isEmpty()) {
            this.name = idCardResponse.getName().trim();
        }

        if (idCardResponse.getPersonalNum() != null && !idCardResponse.getPersonalNum().trim().isEmpty()) {
            try {
                String birthDate = PersonalNumberParser.parseBirthDate(idCardResponse.getPersonalNum());
                Gender gender = PersonalNumberParser.parseGender(idCardResponse.getPersonalNum());

                this.birth = birthDate;
                this.gender = gender;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public void changeActorStatus(ActorStatus actorStatus) {
        this.actorStatus = actorStatus;
    }
    public void changeSuspension(ActorSuspension suspension) {
        this.suspension = suspension;
    }

}