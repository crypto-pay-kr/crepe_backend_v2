package dev.crepe.domain.core.account.repository;

import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByActor_EmailAndCoin_Currency(String email, String currency);

    Optional<Account> findByBank_EmailAndCoin_Currency(String email, String currency);



    List<Account> findByActor_Email(String email);

    List<Account> findByBank_Email(String email);

    Page<Account> findByAddressRegistryStatus(AddressRegistryStatus status, Pageable pageable);
}
