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
import dev.crepe.global.error.exception.ExceptionDbService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TransferServiceImpl  implements TransferService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final ExceptionDbService exceptionDbService;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ActorRepository actorRepository;

    @Override
    @Transactional
    public void requestTransfer(GetTransferRequest request, String email) {
       log.info("송금요청 시작 : {}", email);
        Account senderAccount = accountRepository.findByActor_EmailAndCoin_Currency(email, request.getCurrency())
                .orElseGet(()->accountRepository.findByActor_EmailAndBankToken_Currency(email, request.getCurrency())
                        .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001")));

        Account receiverAccount = accountRepository.findByActor_EmailAndCoin_Currency(request.getReceiverEmail(), request.getCurrency())
                .orElseGet(()->accountRepository.findByActor_EmailAndBankToken_Currency(request.getReceiverEmail(), request.getCurrency())
                        .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001")));

        Actor receiverActor = actorRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(()-> exceptionDbService.getException("ACTOR_002"));

        Actor senderActor = actorRepository.findByEmail(email)
                .orElseThrow(()-> exceptionDbService.getException("ACTOR_002"));

        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw exceptionDbService.getException("ACCOUNT_006");
        }

        accountService.validateAndReduceAmount(senderAccount, request.getAmount());
        receiverAccount.addAmount(request.getAmount());


        TransactionHistory senderHistory = TransactionHistory.builder()
                .account(senderAccount)
                .amount(request.getAmount().negate())
                .afterBalance(senderAccount.getBalance())
                .transactionId(receiverActor.getName())
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.TRANSFER)
                .build();


        TransactionHistory receiverHistory = TransactionHistory.builder()
                .account(receiverAccount)
                .amount(request.getAmount())
                .afterBalance(receiverAccount.getBalance())
                .transactionId(senderActor.getName())
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.TRANSFER)
                .build();

        transactionHistoryRepository.save(senderHistory);
        transactionHistoryRepository.save(receiverHistory);


    }
}
