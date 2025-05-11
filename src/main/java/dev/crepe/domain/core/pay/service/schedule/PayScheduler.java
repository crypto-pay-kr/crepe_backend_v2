package dev.crepe.domain.core.pay.service.schedule;

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
public class PayScheduler {

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
                TransactionStatus.PENDING, TransactionType.PAY,threeDaysAgo
        );

        for (TransactionHistory history : scheduledPayments) {
            try {
                // 정산 완료 처리
                history.acceptedTransactionStatus();

                // 금액 입금 (스토어 계좌에)
                history.getAccount().addAmount(history.getAmount());

                // afterBalance 저장
                history.updateAfterBalance(history.getAccount().getBalance());

                // 저장
                transactionHistoryRepository.save(history);
                accountRepository.save(history.getAccount());
            } catch (Exception e) {
                log.error(" 입금 처리 실패", e);
            }
        }
    }
}