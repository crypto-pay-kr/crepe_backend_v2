package dev.crepe.domain.auth.jwt.repository;

import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<JwtToken, Long> {
}
