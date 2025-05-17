package dev.crepe.domain.core.util.coin.regulation.repository;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findAllByBankToken_Currency(String currency);
    List<Portfolio> findByBankToken(BankToken bankToken);

    void deleteAllByBankToken(BankToken bankToken);

    Optional<Portfolio> findByBankTokenAndCoinCurrency(BankToken bankToken, String currency );
}
