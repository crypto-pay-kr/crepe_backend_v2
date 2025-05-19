package dev.crepe.domain.core.util.history.business.repository;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    boolean existsByTransactionId(String transactionId);
    List<TransactionHistory> findByStatusAndTypeAndCreatedAtBefore(
            TransactionStatus status,
            TransactionType type,
            LocalDateTime createdAtBefore
    );
    List<TransactionHistory> findByStatusAndType(TransactionStatus status, TransactionType type);
    List<TransactionHistory> findByAccount_Id(Long accountId);

    List<TransactionHistory> findAllByPayHistory_Order(Order order);



}
