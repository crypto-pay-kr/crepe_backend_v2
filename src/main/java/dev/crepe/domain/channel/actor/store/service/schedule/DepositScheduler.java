package dev.crepe.domain.channel.actor.store.service.schedule;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositScheduler {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AccountRepository accountRepository;

    /**
     * 00시마다, 3일 지난 PENDING 상태의 정산을 처리함
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkPendingDeposit() {

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<TransactionHistory> scheduledPayments = transactionHistoryRepository.findByStatusAndTypeAndCreatedAtBefore(
                TransactionStatus.PENDING, TransactionType.SETTLEMENT,threeDaysAgo
        );

        for (TransactionHistory history : scheduledPayments) {
            try {
                history.acceptedTransactionStatus();
                history.getAccount().addAmount(history.getAmount());
                transactionHistoryRepository.save(history);
                accountRepository.save(history.getAccount());
                log.info(" 입금 완료 ");
            } catch (Exception e) {
                log.error(" 입금 처리 실패", e);
            }
        }
    }
}