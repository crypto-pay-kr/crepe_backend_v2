package dev.crepe.domain.core.transfer.service.Impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;
import dev.crepe.domain.core.transfer.service.TransferService;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.global.service.impl.HistoryServiceImpl;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.util.RedisDeduplicationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final ExceptionDbService exceptionDbService;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ActorRepository actorRepository;
    private final HistoryServiceImpl historyService;

    @Override
    @Transactional
    public void requestTransfer(GetTransferRequest request, String email) {
        log.info("송금 요청 시작: {}", email);

        Account senderAccount = findLockedAccount(email, request.getCurrency());
        Account receiverAccount = findLockedAccount(request.getReceiverEmail(), request.getCurrency());

        Actor senderActor = actorRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        Actor receiverActor = actorRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        validateBalance(senderAccount, request.getAmount());
        moveBalance(senderAccount, receiverAccount, request.getAmount());
        saveTransferHistories(senderAccount, receiverAccount, senderActor, receiverActor, request.getAmount());
        historyService.invalidateTransactionHistoryCache(email, request.getCurrency());

    }



    private Account findLockedAccount(String email, String currency) {
        return accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .or(() -> accountRepository.findTokenAccountWithLock(email, currency))
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));
    }


    private void validateBalance(Account sender, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0) {
            throw exceptionDbService.getException("ACCOUNT_006");
        }
    }

    private void moveBalance(Account sender, Account receiver, BigDecimal amount) {
        accountService.validateAndReduceAmount(sender, amount);
        receiver.addAmount(amount);
    }

    private void saveTransferHistories(Account sender, Account receiver, Actor senderActor, Actor receiverActor, BigDecimal amount) {
        TransactionHistory senderHistory = TransactionHistory.builder()
                .account(sender)
                .amount(amount.negate())
                .afterBalance(sender.getBalance())
                .transactionId(receiverActor.getName())
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.TRANSFER)
                .build();

        TransactionHistory receiverHistory = TransactionHistory.builder()
                .account(receiver)
                .amount(amount)
                .afterBalance(receiver.getBalance())
                .transactionId(senderActor.getName())
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.TRANSFER)
                .build();

        transactionHistoryRepository.save(senderHistory);
        transactionHistoryRepository.save(receiverHistory);

    }
}