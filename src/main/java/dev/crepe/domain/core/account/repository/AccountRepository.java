package dev.crepe.domain.core.account.repository;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByActor_EmailAndCoin_Currency(String email, String currency);
    Optional<Account> findByActor_EmailAndBankToken_Currency(String email, String currency);
    Optional<Account> findByBankToken_CurrencyAndActorIsNull(String currency);
    Optional<Account> findByBank_EmailAndCoin_Currency(String email, String currency);
    boolean existsByActorEmailAndCoinIdAndAddressRegistryStatus(String email, Long coinId, AddressRegistryStatus status);
    List<Account> findByActor_EmailAndAddressRegistryStatus(String email, AddressRegistryStatus status);
    List<Account> findByActor_IdAndCoin_IdInAndAddressRegistryStatus(Long actorId, List<Long> coinIds, AddressRegistryStatus status);
    List<Account> findByActor_Email(String email);
    List<Account> findByActor_Id(Long id);
    Page<Account> findByActor_Id(Long id, Pageable pageable);
    List<Account> findByBank_Email(String email);
    Page<Account> findByActorIsNotNullAndAddressRegistryStatusInAndCoinIsNotNull(List<AddressRegistryStatus> status, Pageable pageable);
    Page<Account> findByActorIsNullAndAddressRegistryStatusInAndCoinIsNotNull(List<AddressRegistryStatus> status, Pageable pageable);
    Optional<Account> findByActor_EmailAndBankTokenId(String email, Long bankTokenId);
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

    Optional<Account> findByBankAndBankToken(Bank bank, BankToken bankToken);

    boolean existsByAccountAddress(String accountAddress);

    Optional<Account> findByBankTokenIdAndActorIsNull(Long bankTokenId);
  
    List<Account> findByBank_IdAndCoin_IdIn(Long BankId, List<Long> coinIds);

    // 1. BankToken이 연결된 모든 계좌 조회
    List<Account> findByBankTokenIdIsNotNullAndBankIdIsNotNull();

    // 2. 특정 이메일을 가진 유저의 BankToken 계좌 조회
    List<Account> findByActor_EmailAndBankTokenIdIsNotNull(String email);


    @Query("""
    SELECT a FROM Account a
    WHERE a.actor.email = :email
    AND a.accountAddress IS NOT NULL
    """)
    List<Account> findByActor_EmailAndAddress(@Param("email") String email);

    @Query("""
    SELECT a FROM Account a
    WHERE a.actor.id = :actorId
    AND a.coin.id IN :coinIds
    AND a.accountAddress IS NOT NULL
    """)
    List<Account> findByActor_IdAndCoin_IdInAndAddress(@Param("actorId") Long actorId, @Param("coinIds") List<Long> coinIds);

    // AccountRepository.java
    @Query("""
    SELECT a FROM Account a
    WHERE a.actor = :actor
    AND a.bankToken = :bankToken
""")
    Optional<Account> findByActorAndBankToken(@Param("actor") Actor actor, @Param("bankToken") BankToken bankToken);

    @Query("""
    SELECT a FROM Account a
    WHERE a.actor.id = :storeId
    AND a.bankToken IS NOT NULL
""")
    List<Account> findByStoreIdAndBankTokenIsNotNull(@Param("storeId") Long storeId);

    @Query("""
    SELECT COUNT(a) > 0 FROM Account a
    WHERE a.actor.email = :email
    AND a.coin.id = :coinId
    """)
    boolean existsByActorEmailAndCoinId(@Param("email") String email, @Param("coinId") Long coinId);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.actor.email = :email AND a.coin.currency = :currency")
    Optional<Account> findCoinAccountWithLock(@Param("email") String email, @Param("currency") String currency);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.actor.email = :email AND a.bankToken.currency = :currency")
    Optional<Account> findTokenAccountWithLock(@Param("email") String email, @Param("currency") String currency);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.bankToken.currency = :currency and a.actor is null")
    Optional<Account> findBankTokenAccountWithLock(String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.bank.id = :bankId and a.coin.id in :coinIds")
    List<Account> findByBankCoinAccountWithLock(Long bankId, List<Long> coinIds);



}
