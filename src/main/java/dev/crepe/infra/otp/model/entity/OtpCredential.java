package dev.crepe.infra.otp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_credentials")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OtpCredential {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "secret_key", nullable = false, length = 255)
    private String secretKey;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OtpCredential(Long userId, String secretKey) {
        this.userId = userId;
        this.secretKey = secretKey;
        this.enabled = false; // 초기에는 비활성화 상태
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSecretKey(String secretKey) {
        this.secretKey = secretKey;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
}
