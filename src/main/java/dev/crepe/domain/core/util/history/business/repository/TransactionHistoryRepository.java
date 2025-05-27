package dev.crepe.domain.core.util.history.business.repository;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.dto.CoinUsageDto;
import dev.crepe.domain.core.util.history.business.model.dto.PayMonthlyAmountDto;
import dev.crepe.domain.core.util.history.business.model.dto.PayStatusCountDto;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query("""
    SELECT COALESCE(SUM(th.amount), 0)
    FROM TransactionHistory th
    JOIN th.account acc
    JOIN acc.actor actor
    WHERE actor.role = 'USER' AND th.status = 'ACCEPTED'
""")
    BigDecimal sumTransactionAmountByUserRole();

    @Query("""
  SELECT new dev.crepe.domain.core.util.history.business.model.dto.CoinUsageDto(acc.coin.currency, SUM(th.amount))
  FROM TransactionHistory th
  JOIN th.account acc
  JOIN acc.actor actor
  WHERE th.status = 'ACCEPTED'
    AND actor.role = 'USER'
  GROUP BY acc.coin.currency
""")
    List<CoinUsageDto> getUsageByCoinFiltered();


    @Query("""
    SELECT new dev.crepe.domain.core.util.history.business.model.dto.PayMonthlyAmountDto(
        MONTH(th.createdAt), 
        SUM(th.amount)
    )
    FROM TransactionHistory th
    WHERE th.status = dev.crepe.domain.core.util.history.business.model.TransactionStatus.ACCEPTED
        AND th.type = dev.crepe.domain.core.util.history.business.model.TransactionType.PAY
        AND th.account.actor.email = :email
    GROUP BY MONTH(th.createdAt)
    ORDER BY MONTH(th.createdAt)
""")
    List<PayMonthlyAmountDto> findMonthlyAcceptedTransactionTotalsByEmail(@Param("email") String email);

    @Query("""
    SELECT new dev.crepe.domain.core.util.history.business.model.dto.PayMonthlyAmountDto(
        MONTH(th.createdAt), 
        SUM(th.amount)
    )
    FROM TransactionHistory th
    WHERE th.status = dev.crepe.domain.core.util.history.business.model.TransactionStatus.ACCEPTED
        AND th.type = dev.crepe.domain.core.util.history.business.model.TransactionType.PAY
        AND th.account.actor.email = :email
    GROUP BY MONTH(th.createdAt)
    ORDER BY MONTH(th.createdAt)
""")
    List<PayMonthlyAmountDto> findMonthlyAcceptedTransactionTotalsByEmail();



    @Query("""
    SELECT new dev.crepe.domain.core.util.history.business.model.dto.PayStatusCountDto(
        th.status,
        COUNT(th)
    )
    FROM TransactionHistory th
    WHERE th.type = dev.crepe.domain.core.util.history.business.model.TransactionType.PAY
        AND th.account.actor.email = :email
    GROUP BY th.status
""")
    List<PayStatusCountDto> countTotalByStatus(@Param("email") String email);
}
