package dev.crepe.domain.channel.actor.user.model.entity;

import dev.crepe.global.base.UserBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="user")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User extends UserBaseEntity {
    @Column(name="nick_name", nullable = false)
    private String nickName;

}
