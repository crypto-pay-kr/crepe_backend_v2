package dev.crepe.domain.core.util.history.transfer.repository;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    boolean existsByTransactionId(String transactionId);
    List<TransactionHistory> findByStatusAndTypeAndCreatedAtBefore(
            TransactionStatus status,
            TransactionType type,
            LocalDateTime createdAtBefore
    );
    List<TransactionHistory> findByStatusAndType(TransactionStatus status, TransactionType type);
    List<TransactionHistory> findByAccount_IdOrderByCreatedAtDesc(Long accountId);

    List<TransactionHistory> findAllByPayHistory_Order(Order order);

}
