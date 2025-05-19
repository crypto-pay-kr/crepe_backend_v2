package dev.crepe.infra.otp.repository;

import dev.crepe.infra.otp.model.entity.OtpCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCredentialRepository extends JpaRepository<OtpCredential, Long> {
    Optional<OtpCredential> findByUserId(Long userId);
}
