package dev.crepe.domain.core.transfer.service.Impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
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

    private static final int MAX_RETRY_COUNT = 5; // 입금 상태 확인을 재시도할 최대 횟수
    private static final int RETRY_INTERVAL_MS = 1000; // 재시도 간격(ms)

    private final AccountRepository accountRepository;
    private final CoinRepository coinRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UpbitDepositService upbitDepositService;
    private final ActorRepository actorRepository;

    @Override
    public void requestDeposit(GetDepositRequest request, String email) {
        String txid = request.getTxid();
        String currency = request.getCurrency();

        // 1. 코인 정보 조회
        Coin coin = coinRepository.findByCurrency(currency);

        Actor actor = actorRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException());

        // 2. 해당 이메일, 코인에 해당하는 계좌 조회 하고 없으면 계좌 생성
        Account account = accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .orElseGet(() -> accountRepository.save(
                        Account.builder()
                                .actor(actor)
                                .coin(coin)
                                .balance(BigDecimal.ZERO)
                                .accountAddress(null)
                                .tag(null)
                                .addressRegistryStatus(AddressRegistryStatus.NOT_REGISTERED)
                                .build()
                ));

        // 3. 이미 처리된 txid인지 확인
        if (transactionHistoryRepository.existsByTransactionId(txid)) {
            log.warn("이미 처리된 거래입니다. txid={}", txid);
            throw new DuplicateTransactionException(txid);
        }

        // 4. 업비트 API로 입금 내역 조회
        List<GetDepositResponse> depositList = upbitDepositService.getDepositListById(currency, txid);
        if (depositList.isEmpty()) {
            throw new DepositNotFoundException(txid);
        }


        // 5. 중복 입금 안되도록 방지
        if (depositList.size() != 1) {
            log.warn("중복된 입금 내역 존재. txid={}, count={}", txid, depositList.size());
            throw new IllegalStateException("유효하지 않은 거래: 중복 입금 내역 존재");
        }


        GetDepositResponse deposit = depositList.get(0);
        BigDecimal amount = new BigDecimal(deposit.getAmount());

        if (!deposit.getCurrency().equalsIgnoreCase(currency)) {
            log.warn("입금된 코인과 요청 코인이 일치하지 않음. 요청={}, 실제={}", currency, deposit.getCurrency());
            throw new CurrencyMismatchException(currency, deposit.getCurrency(), txid);
        }

        TransactionHistory history = null;

        // 6. 상태가 ACCEPTED 인지 최대 5번 재시도하면서 확인
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            depositList = upbitDepositService.getDepositListById(currency, txid);
            if (depositList.isEmpty()) {
                throw new IllegalStateException("재조회했지만 입금 내역이 없음");
            }

            deposit = depositList.get(0);

            // ACCEPTED 상태면 계좌에 금액 추가 및 내역 저장
            if (TransactionStatus.ACCEPTED.name().equals(deposit.getState())) {
                account.addAmount(amount);
                history = TransactionHistory.builder()
                        .account(account)
                        .amount(amount)
                        .transactionId(txid)
                        .status(TransactionStatus.ACCEPTED)
                        .type(TransactionType.DEPOSIT)
                        .build();
                break;
            }

            // ACCEPTED 상태가 아닐 경우 대기
            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("입금 상태 확인 중 인터럽트 발생");
                break;
            }
        }

        // 7. ACCEPTED로 전환되지 못했다면 FAILED 상태로 저장
        if (history == null) {
            history = TransactionHistory.builder()
                    .account(account)
                    .amount(amount)
                    .transactionId(txid)
                    .status(TransactionStatus.FAILED)
                    .type(TransactionType.DEPOSIT)
                    .build();
        }

        // 8. 입금 내역 저장
        transactionHistoryRepository.save(history);
    }
}