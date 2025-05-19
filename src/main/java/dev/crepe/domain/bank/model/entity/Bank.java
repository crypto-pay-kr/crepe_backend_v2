package dev.crepe.domain.bank.model.entity;

import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="bank")
@Getter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bank extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(name = "bank_phone_num",nullable = false)
    private String bankPhoneNum;

    @Column(nullable = false)
    private String imageUrl;

    @Column(name="bank_code",nullable = false)
    private String bankCode;

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public void changePhoneNum(String newPhone) {
        this.bankPhoneNum=newPhone;
    }

}
