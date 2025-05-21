package dev.crepe.domain.bank.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.model.entity.BankStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    boolean existsByBankCode(String bankCode);

    List<Bank> findByStatus(BankStatus status);
}
