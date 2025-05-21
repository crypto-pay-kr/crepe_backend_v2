package dev.crepe.domain.core.account.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByActor_EmailAndCoin_Currency(String email, String currency);
    Optional<Account> findByActor_EmailAndBankToken_Currency(String email, String currency);
    Optional<Account> findByBankToken_CurrencyAndActorIsNull(String currency);
    Optional<Account> findByBank_EmailAndCoin_Currency(String email, String currency);
    List<Account> findByActor_Email(String email);
    List<Account> findByBank_Email(String email);
    Page<Account> findByAddressRegistryStatusIn(List<AddressRegistryStatus> status, Pageable pageable);
  
    Optional<Account> findByActor_EmailAndBankTokenId(String email, Long bankTokenId);

    Optional<Account> findByBankAndBankTokenAndAddressRegistryStatus(
            Bank bank,
            BankToken bankToken,
            AddressRegistryStatus status
    );

    Optional<Account> findByBank_EmailAndCoin_CurrencyAndAddressRegistryStatus(
            String bankEmail,
            String currency,
            AddressRegistryStatus status
    );

    Optional<Account> findByBankIdAndBankTokenAndActorIsNull(Long bankId, BankToken bankToken);

    Optional<Account> findByBankAndCoin(Bank bank, Coin coin);

    Optional<Account> findByBankAndBankToken(Bank bank, BankToken bankToken);

    boolean existsByAccountAddress(String accountAddress);

    Optional<Account> findByBankTokenIdAndActorIsNull(Long bankTokenId);
  
    List<Account> findByBank_IdAndCoin_IdIn(Long BankId, List<Long> coinIds);

}
