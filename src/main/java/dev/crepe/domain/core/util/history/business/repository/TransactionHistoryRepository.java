package dev.crepe.domain.core.util.history.business.repository;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    Slice<TransactionHistory> findByAccount_Id(Long accountId, Pageable pageable);

    List<TransactionHistory> findAllByPayHistory_Order(Order order);

    TransactionHistory findByPayHistory_IdAndAccount_Actor_EmailAndType(
            Long payHistoryId,
            String email,
            TransactionType type
    );
    TransactionHistory findByPayHistory_IdAndAccount_Actor_EmailNotAndType(
            Long payHistoryId,
            String email,
            TransactionType type
    );


}
