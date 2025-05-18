package dev.crepe.domain.core.transfer.service.scheduler;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.transfer.model.dto.response.CheckWithdrawResponse;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
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
                CheckWithdrawResponse result = upbitWithdrawService.checkWithdraw(payment.getTransactionId());

                if (result.isCompleted()) {
                    log.info("[출금 완료] amount={}", result.getAmount());

                    Account account = payment.getAccount();
                    // 실제 출금 처리
                    account.deductBalance(payment.getAmount().abs());
                    // 거래 상태 및 잔액 업데이트
                    payment.acceptedTransactionStatus();
                    payment.updateAfterBalance(account.getBalance());

                    transactionHistoryRepository.save(payment);
                }else {
                    log.info("출금 중");
                }
            } catch (Exception e) {
                log.error("출금 상태 확인 실패: txId={}", payment.getAccount().getActor().getName(), e);
            }
        }
    }
}