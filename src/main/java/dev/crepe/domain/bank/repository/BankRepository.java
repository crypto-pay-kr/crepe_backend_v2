package dev.crepe.domain.bank.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    boolean existsByBankCode(String bankCode);
}
