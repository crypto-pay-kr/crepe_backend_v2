package dev.crepe.domain.core.transfer.service.scheduler;

import dev.crepe.domain.core.transfer.model.dto.response.CheckWithdrawResponse;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.upbit.Service.UpbitWithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WithdrawScheduler {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UpbitWithdrawService upbitWithdrawService;
    /**
     * 3분마다 실행되며, 출금이 완료되면 입금내역에 입금 되었다고 표시됨
     */
    @Scheduled(fixedDelay = 180000)
    public void checkPendingSettlement() {
        List<TransactionHistory> pendingWithdrawals =
                transactionHistoryRepository.findByStatusAndType(TransactionStatus.PENDING, TransactionType.WITHDRAW);
        for (TransactionHistory payment : pendingWithdrawals) {
            try {
                CheckWithdrawResponse result = upbitWithdrawService.checkSettlement(payment.getTransactionId());

                if (result.isCompleted()) {
                    log.info("[출금 완료] amount={}", result.getAmount());

                    payment.acceptedTransactionStatus();

                    transactionHistoryRepository.save(payment);
                }else {
                    log.info("출금 중");
                }
            } catch (Exception e) {
                log.error("출금 상태 확인 실패: txId={}", payment.getAccount().getStore().getName(), e);
            }
        }
    }
}