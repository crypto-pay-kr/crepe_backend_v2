package dev.crepe.domain.core.account.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
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
    List<Account> findByActor_Id(Long id);
    Page<Account> findByActor_Id(Long id, Pageable pageable);
    List<Account> findByBank_Email(String email);
    Page<Account> findByActorIsNotNullAndAddressRegistryStatusInAndCoinIsNotNull(List<AddressRegistryStatus> status, Pageable pageable);
    Page<Account> findByActorIsNullAndAddressRegistryStatusInAndCoinIsNotNull(List<AddressRegistryStatus> status, Pageable pageable);
    Optional<Account> findByActor_EmailAndBankTokenId(String email, Long bankTokenId);
    boolean existsByActorAndBankToken(Actor actor, BankToken bankToken);
    Optional<Account> findByBankAndBankTokenAndAddressRegistryStatus(
            Bank bank,
            BankToken bankToken,
            AddressRegistryStatus status
    );
    boolean existsByActor_EmailAndCoin(String email,Coin coin);
    boolean existsByBankAndCoin(Bank bank, Coin coin);
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

    List<Account> findAllByActor_EmailAndBankToken_Id(String email, Long bankTokenId);



    // 1. BankToken이 연결된 모든 계좌 조회
    List<Account> findByBankTokenIdIsNotNullAndBankIdIsNotNull();

    // 2. 특정 이메일을 가진 유저의 BankToken 계좌 조회
    List<Account> findByActor_EmailAndBankTokenIdIsNotNull(String email);

}
