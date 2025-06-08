package dev.crepe.domain.core.transfer.service.Impl;

import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.model.dto.response.GetWithdrawResponse;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.upbit.Service.UpbitWithdrawService;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.util.RedisDeduplicationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawServiceImpl implements WithdrawService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AccountRepository accountRepository;
    private final UpbitWithdrawService upbitWithdrawService;
    private final CoinRepository coinRepository;
    private final ExceptionDbService exceptionDbService;
    private final RedisDeduplicationUtil redisDeduplicationUtil;

    @Transactional
    @Override
    public void requestWithdraw(GetWithdrawRequest request, String email) {
        log.info("출금 요청 처리 시작: {}", email);


        Account account = findValidAccount(email, request.getCurrency());

        validateNoPendingWithdraw(account);
        validateAddressRegistry(account);

        BigDecimal requestAmount = new BigDecimal(request.getAmount());
        validateSufficientBalance(account, requestAmount);

        Coin coin = coinRepository.findByCurrency(request.getCurrency());

        GetWithdrawResponse response = requestWithdrawToUpbit(request, account, coin);
        saveWithdrawHistory(account, requestAmount, response.getUuid());
    }





    private Account findValidAccount(String email, String currency) {
        return accountRepository.findCoinAccountWithLock(email, currency)
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));
    }

    private void validateNoPendingWithdraw(Account account) {
        boolean existsPending = transactionHistoryRepository.existsByAccountAndStatusAndType(
                account, TransactionStatus.PENDING, TransactionType.WITHDRAW
        );
        if (existsPending) {
            throw exceptionDbService.getException("DUPLICATE_REQUEST_001");
        }
    }

    private void validateAddressRegistry(Account account) {
        if (account.getAddressRegistryStatus() != AddressRegistryStatus.ACTIVE) {
            throw exceptionDbService.getException("ACCOUNT_005");
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal requestAmount) {
        if (account.getBalance().compareTo(requestAmount) < 0) {
            throw exceptionDbService.getException("ACCOUNT_006");
        }
    }

    private GetWithdrawResponse requestWithdrawToUpbit(GetWithdrawRequest request, Account account, Coin coin) {
        GetWithdrawResponse response = upbitWithdrawService.requestWithdraw(
                request,
                account.getAccountAddress(),
                account.getTag(),
                coin.getNetworkType()
        );

        if (response == null || response.getUuid() == null) {
            throw exceptionDbService.getException("WITHDRAW_001");
        }
        return response;
    }

    private void saveWithdrawHistory(Account account, BigDecimal amount, String transactionId) {
        TransactionHistory history = TransactionHistory.builder()
                .account(account)
                .amount(amount.negate())
                .transactionId(transactionId)
                .status(TransactionStatus.PENDING)
                .type(TransactionType.WITHDRAW)
                .build();
        transactionHistoryRepository.save(history);
    }
}
