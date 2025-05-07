package dev.crepe.domain.auth.jwt.repository;

import dev.crepe.domain.auth.model.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<JwtToken, Long> {
}
