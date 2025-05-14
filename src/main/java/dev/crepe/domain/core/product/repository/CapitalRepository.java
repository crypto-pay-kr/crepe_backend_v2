package dev.crepe.domain.core.product.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.product.model.entity.Capital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CapitalRepository extends JpaRepository<Capital, Long> {
    Optional<Capital> findByBank(Bank bank);
}
