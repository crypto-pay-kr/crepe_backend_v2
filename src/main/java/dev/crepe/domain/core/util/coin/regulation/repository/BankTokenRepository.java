package dev.crepe.domain.core.util.coin.regulation.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankTokenRepository extends JpaRepository<BankToken, Long> {
    Optional<BankToken> findByBank(Bank bank);
    Optional<BankToken> findByCurrency(String currency);
    boolean existsByBank_Id(Long bankId);

}
