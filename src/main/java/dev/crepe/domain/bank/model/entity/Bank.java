package dev.crepe.domain.bank.model.entity;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

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

    @Column(nullable = false)
    private String bankPhoneNum;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String bankCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

}
