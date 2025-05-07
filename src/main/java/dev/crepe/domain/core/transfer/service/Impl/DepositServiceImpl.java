package dev.crepe.domain.core.transfer.service.Impl;

import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.transfer.exception.CurrencyMismatchException;
import dev.crepe.domain.core.transfer.exception.DepositNotFoundException;
import dev.crepe.domain.core.transfer.exception.DuplicateTransactionException;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.response.GetDepositResponse;
import dev.crepe.domain.core.transfer.service.DepositService;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.upbit.Service.UpbitDepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {

    private static final int MAX_RETRY_COUNT = 5;
    private static final int RETRY_INTERVAL_MS = 1000;

    private final AccountRepository accountRepository;
    private final CoinRepository coinRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UpbitDepositService upbitDepositService;

    @Override
    public void requestDeposit(GetDepositRequest request, String email) {

        String txid = request.getTxid();
        String currency = request.getCurrency();

        Coin coin = coinRepository.findByCurrency(currency);

        Optional<Account> optionalAccount = accountRepository.findByUser_EmailAndCoin_Currency(email, currency);
        if (optionalAccount.isEmpty()) {
            optionalAccount = accountRepository.findByStore_EmailAndCoin_Currency(email, currency);
        }

        Account account = optionalAccount.orElseGet(() -> {
            Account newAccount = Account.builder()
                    .coin(coin)
                    .balance(BigDecimal.ZERO)
                    .accountAddress(null)
                    .tag(null)
                    .addressRegistryStatus(AddressRegistryStatus.NOT_REGISTERED)
                    .build();
            return accountRepository.save(newAccount);
        });

        if (transactionHistoryRepository.existsByTransactionId(txid)) {
            log.warn("이미 처리된 거래입니다. txid={}", txid);
            throw new DuplicateTransactionException(txid);
        }

        List<GetDepositResponse> depositList = upbitDepositService.getDepositListById(currency, txid);
        if (depositList.isEmpty()) {
            throw new DepositNotFoundException(txid);
        }

        if (depositList.size() != 1) {
            log.warn("거래 ID에 여러 개의 입금 내역이 존재합니다. txid={}, count={}", txid, depositList.size());
            throw new IllegalStateException("유효하지 않은 거래: 중복 입금 내역 존재");
        }

        GetDepositResponse deposit = depositList.get(0);
        BigDecimal amount = new BigDecimal(deposit.getAmount());

        if (!deposit.getCurrency().equalsIgnoreCase(currency)) {
            log.warn("요청한 코인과 실제 입금된 코인이 일치하지 않습니다. 요청: {}, 실제: {}, txid: {}", currency, deposit.getCurrency(), txid);
            throw new CurrencyMismatchException(currency, deposit.getCurrency(), txid);
        }

        TransactionHistory history = null;

        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            depositList = upbitDepositService.getDepositListById(currency, txid);
            if (depositList.isEmpty()) {
                throw new IllegalStateException("입금 내역을 재조회했지만 여전히 찾을 수 없습니다.");
            }

            deposit = depositList.get(0);

            if (TransactionStatus.ACCEPTED.name().equals(deposit.getState())) {
                account.addAmount(amount);

                TransactionStatus status = (account.getStore() != null)
                        ? TransactionStatus.PENDING
                        : TransactionStatus.ACCEPTED;

                history = TransactionHistory.builder()
                        .account(account)
                        .amount(amount)
                        .transactionId(txid)
                        .status(status)
                        .type(TransactionType.DEPOSIT)
                        .build();
                break;
            }

            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("입금 상태 확인 중 인터럽트 발생");
                break;
            }
        }

        if (history == null) {
            history = TransactionHistory.builder()
                    .account(account)
                    .amount(amount)
                    .transactionId(txid)
                    .status(TransactionStatus.FAILED)
                    .type(TransactionType.DEPOSIT)
                    .build();
        }

        transactionHistoryRepository.save(history);
    }
}