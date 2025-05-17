package dev.crepe.domain.core.util.coin.regulation.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankTokenRepository extends JpaRepository<BankToken, Long> {
    Optional<BankToken> findByBank(Bank bank);

    Page<BankToken> findByBank_Id(Long bankId, Pageable pageable);

    boolean existsByBank_Id(Long bankId);


}
