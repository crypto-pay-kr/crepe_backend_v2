package dev.crepe.domain.auth.otp.repository;

import dev.crepe.domain.auth.otp.model.entity.OtpCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCredentialRepository extends JpaRepository<OtpCredential, Long> {
    Optional<OtpCredential> findByUserId(Long userId);
}
