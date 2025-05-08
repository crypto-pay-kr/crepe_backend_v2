package dev.crepe.domain.core.transfer.service.Impl;

import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.transfer.exception.AccountNotRegisteredException;
import dev.crepe.domain.core.transfer.exception.NotEnoughAmountException;
import dev.crepe.domain.core.transfer.exception.WithdrawRequestFailedException;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.model.dto.response.GetWithdrawResponse;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.upbit.Service.UpbitWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AccountRepository accountRepository;
    private final UpbitWithdrawService upbitWithdrawService;
    private final CoinRepository coinRepository;


    @Transactional
    @Override
    public void requestWithdraw(GetWithdrawRequest request,String email) {

        // 1.계좌 조회
        Account account = accountRepository.findByActor_EmailAndCoin_Currency(email, request.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(email));

        // 2. 주소 등록 여부 확인
        if (account.getAddressRegistryStatus() != AddressRegistryStatus.ACTIVE) {
            throw new AccountNotRegisteredException();
        }

        // 3. 코인 정보 확인
        Coin coin = coinRepository.findByCurrency(request.getCurrency());

        BigDecimal requestAmount = new BigDecimal(request.getAmount());
        validateSufficientBalance(account, requestAmount);

        // 4. 업비트 출금 요청
        GetWithdrawResponse response = upbitWithdrawService.requestWithdraw(
                request,
                account.getAccountAddress(),
                account.getTag(),
                coin.getNetworkType()
        );

        if (response == null || response.getUuid() == null) {
            throw new WithdrawRequestFailedException();
        }

        // 5. 계좌 금액 차감
        account.reduceAmount(requestAmount);

        // 6. 거래 내역 저장 (공통)
        TransactionHistory history = TransactionHistory.builder()
                .account(account)
                .amount(requestAmount.negate())
                .transactionId(response.getUuid())
                .status(TransactionStatus.PENDING)
                .type(TransactionType.WITHDRAW)
                .build();

        transactionHistoryRepository.save(history);
    }

    private void validateSufficientBalance(Account account, BigDecimal requestAmount) {
        if (account.getBalance().compareTo(requestAmount) < 0) {
            throw new NotEnoughAmountException(account.getCoin().getCurrency());
        }
    }


}