package dev.crepe.domain.core.util.history.token.repository;

import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {
    Optional<TokenHistory> findByBankTokenAndStatus(BankToken bankToken, BankTokenStatus status);


}
