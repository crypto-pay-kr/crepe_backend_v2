package dev.crepe.domain.channel.actor.store.service.impl;


import dev.crepe.domain.channel.actor.store.service.StoreDepositService;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreDepositServiceImpl implements StoreDepositService {

   private final AccountRepository accountRepository;
   private final TransactionHistoryRepository transactionHistoryRepository;

    @Override
    public void userWithdrawForOrder(Order order) {

        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getUser().getEmail(),order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getUser().getEmail()));


        TransactionHistory history = TransactionHistory.builder()
                .account(userAccount)
                //.amount(order.getTotalPrice()) TODO 환전 기능 추가 후 수정
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.WITHDRAW)
                .account(userAccount)
                .build();

        transactionHistoryRepository.save(history);

    }


    @Override
    public void pendingStoreDepositForOrder(Order order, Long storeId) {

        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getUser().getEmail(),order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getUser().getEmail()));



        TransactionHistory history = TransactionHistory.builder()
                .account(userAccount)
                //.amount(order.getTotalPrice())  // TODO 환전 기능 추가 후 수정
                .status(TransactionStatus.PENDING)
                .type(TransactionType.SETTLEMENT)
                .account(userAccount)
                .build();

        transactionHistoryRepository.save(history);

    }
}
