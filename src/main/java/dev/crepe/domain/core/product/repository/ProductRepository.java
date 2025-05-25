package dev.crepe.domain.core.product.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    List<Product> findByBank(Bank bank);

    Optional<Product> findByIdAndBank(Long id, Bank bank);

    List<Product> findByBankId(Long bankId);

    List<Product> findByBankAndStatus(Bank bank, BankProductStatus status);

}
