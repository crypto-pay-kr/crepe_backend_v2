// package dev.crepe.domain.core.pay;

// import dev.crepe.domain.channel.actor.model.entity.Actor;
// import dev.crepe.domain.channel.market.order.model.OrderStatus;
// import dev.crepe.domain.channel.market.order.model.OrderType;
// import dev.crepe.domain.channel.market.order.model.entity.Order;
// import dev.crepe.domain.core.account.exception.AccountNotFoundException;
// import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
// import dev.crepe.domain.core.account.model.entity.Account;
// import dev.crepe.domain.core.account.repository.AccountRepository;
// import dev.crepe.domain.core.pay.exception.AlreadyRefundException;
// import dev.crepe.domain.core.pay.exception.StoreAlreadySettledException;
// import dev.crepe.domain.core.pay.service.impl.PayServiceImpl;
// import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
// import dev.crepe.domain.core.util.history.pay.execption.PayHistoryNotFoundException;
// import dev.crepe.domain.core.util.history.pay.model.PayType;
// import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
// import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
// import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
// import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
// import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
// import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.math.BigDecimal;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class PayServiceImplTest {

//     @Mock
//     private AccountRepository accountRepository;

//     @Mock
//     private TransactionHistoryRepository transactionHistoryRepository;

//     @Mock
//     private PayHistoryRepository payHistoryRepository;

//     @InjectMocks
//     private PayServiceImpl payService;

//     @Test
//     @DisplayName("주문에 대한 결제 성공 테스트")
//     void payForOrder_Success() {
//         // given
//         String userEmail = "user@example.com";
//         String storeEmail = "store@example.com";
//         String currency = "BTC";
//         BigDecimal exchangeRate = new BigDecimal("40000000"); // 4천만원
//         int totalPrice = 100000; // 10만원
//         BigDecimal expectedAmount = new BigDecimal("0.0025"); // 10만원 / 4천만원 = 0.0025 BTC

//         Order order = createOrder(userEmail, storeEmail, currency, exchangeRate, totalPrice);
        
//         Account userAccount = createAccount(1L, currency, new BigDecimal("1.0")); // 충분한 잔액
//         Account storeAccount = createAccount(2L, currency, BigDecimal.ZERO);
        
//         when(accountRepository.findByActor_EmailAndCoin_Currency(userEmail, currency))
//                 .thenReturn(Optional.of(userAccount));
//         when(accountRepository.findByActor_EmailAndCoin_Currency(storeEmail, currency))
//                 .thenReturn(Optional.of(storeAccount));
//         when(payHistoryRepository.save(any(PayHistory.class)))
//                 .thenAnswer(invocation -> invocation.getArgument(0));
//         when(transactionHistoryRepository.save(any(TransactionHistory.class)))
//                 .thenAnswer(invocation -> invocation.getArgument(0));

//         // when
//         payService.payForOrder(order);

//         // then
//         verify(accountRepository).findByActor_EmailAndCoin_Currency(userEmail, currency);
//         verify(accountRepository).findByActor_EmailAndCoin_Currency(storeEmail, currency);
//         verify(payHistoryRepository).save(any(PayHistory.class));
//         verify(transactionHistoryRepository, times(2)).save(any(TransactionHistory.class));
        
//         // 사용자 계정에서 금액이 차감되었는지 확인
//         assertEquals(new BigDecimal("0.9975"), userAccount.getBalance());
//         assertEquals(new BigDecimal("0.9975"), userAccount.getAvailableBalance());
//     }

//     @Test
//     @DisplayName("잔액 부족 시 결제 실패 테스트")
//     void payForOrder_InsufficientBalance() {
//         // given
//         String userEmail = "user@example.com";
//         String storeEmail = "store@example.com";
//         String currency = "BTC";
//         BigDecimal exchangeRate = new BigDecimal("40000000"); // 4천만원
//         int totalPrice = 100000; // 10만원
//         BigDecimal expectedAmount = new BigDecimal("0.0025"); // 10만원 / 4천만원 = 0.0025 BTC

//         Order order = createOrder(userEmail, storeEmail, currency, exchangeRate, totalPrice);
        
//         Account userAccount = createAccount(1L, currency, new BigDecimal("0.001")); // 부족한 잔액
        
//         when(accountRepository.findByActor_EmailAndCoin_Currency(userEmail, currency))
//                 .thenReturn(Optional.of(userAccount));

//         // when & then
//         assertThrows(NotEnoughAmountException.class, () -> payService.payForOrder(order));
        
//         verify(accountRepository).findByActor_EmailAndCoin_Currency(userEmail, currency);
//         verify(payHistoryRepository, never()).save(any(PayHistory.class));
//         verify(transactionHistoryRepository, never()).save(any(TransactionHistory.class));
//     }

//     @Test
//     @DisplayName("주문 취소 성공 테스트")
//     void cancelForOrder_Success() {
//         // given
//         String userEmail = "user@example.com";
//         String storeEmail = "store@example.com";
//         String currency = "BTC";
//         BigDecimal amount = new BigDecimal("0.0025");

//         Order order = createOrder(userEmail, storeEmail, currency, BigDecimal.valueOf(40000000), 100000);
        
//         Account userAccount = createAccount(1L, currency, new BigDecimal("0.9975"));
//         Account storeAccount = createAccount(2L, currency, BigDecimal.ZERO);
        
//         PayHistory payHistory = PayHistory.builder()
//                 .id(1L)
//                 .order(order)
//                 .status(PayType.PENDING)
//                 .totalAmount(amount)
//                 .build();
        
//         TransactionHistory userTx = TransactionHistory.builder()
//                 .id(1L)
//                 .account(userAccount)
//                 .payHistory(payHistory)
//                 .amount(amount.negate())
//                 .status(TransactionStatus.ACCEPTED)
//                 .type(TransactionType.PAY)
//                 .build();
        
//         TransactionHistory storeTx = TransactionHistory.builder()
//                 .id(2L)
//                 .account(storeAccount)
//                 .payHistory(payHistory)
//                 .amount(amount)
//                 .status(TransactionStatus.PENDING)
//                 .type(TransactionType.PAY)
//                 .build();
        
//         List<TransactionHistory> txList = Arrays.asList(userTx, storeTx);
        
//         when(accountRepository.findByActor_EmailAndCoin_Currency(userEmail, currency))
//                 .thenReturn(Optional.of(userAccount));
//         when(accountRepository.findByActor_EmailAndCoin_Currency(storeEmail, currency))
//                 .thenReturn(Optional.of(storeAccount));
//         when(payHistoryRepository.findByOrder(order))
//                 .thenReturn(Optional.of(payHistory));
//         when(transactionHistoryRepository.findAllByPayHistory_Order(order))
//                 .thenReturn(txList);

//         // when
//         payService.cancelForOrder(order);

//         // then
//         verify(payHistoryRepository).findByOrder(order);
//         verify(transactionHistoryRepository).findAllByPayHistory_Order(order);
//         verify(payHistoryRepository).save(payHistory);
//         verify(transactionHistoryRepository, times(2)).save(any(TransactionHistory.class));
        
//         assertEquals(PayType.CANCELED, payHistory.getStatus());
//         assertEquals(TransactionStatus.FAILED, userTx.getStatus());
//         assertEquals(TransactionStatus.FAILED, storeTx.getStatus());
//         assertEquals(new BigDecimal("1.0"), userAccount.getBalance()); // 환불된 금액 확인
//         assertEquals(new BigDecimal("1.0"), userAccount.getAvailableBalance());
//     }

//     @Test
//     @DisplayName("환불 성공 테스트")
//     void refundForOrder_Success() {
//         // given
//         Long payId = 1L;
//         String storeEmail = "store@example.com";
        
//         Account userAccount = createAccount(1L, "BTC", new BigDecimal("0.9975"));
//         Account storeAccount = createAccount(2L, "BTC", BigDecimal.ZERO);
        
//         PayHistory payHistory = PayHistory.builder()
//                 .id(payId)
//                 .status(PayType.APPROVED)
//                 .totalAmount(new BigDecimal("0.0025"))
//                 .build();
        
//         TransactionHistory userTx = TransactionHistory.builder()
//                 .id(1L)
//                 .account(userAccount)
//                 .payHistory(payHistory)
//                 .amount(new BigDecimal("-0.0025"))
//                 .status(TransactionStatus.ACCEPTED)
//                 .type(TransactionType.PAY)
//                 .build();
        
//         TransactionHistory storeTx = TransactionHistory.builder()
//                 .id(2L)
//                 .account(storeAccount)
//                 .payHistory(payHistory)
//                 .amount(new BigDecimal("0.0025"))
//                 .status(TransactionStatus.PENDING) // 아직 정산되지 않은 상태
//                 .type(TransactionType.PAY)
//                 .build();
        
//         when(payHistoryRepository.findById(payId)).thenReturn(Optional.of(payHistory));
//         when(transactionHistoryRepository.save(any(TransactionHistory.class)))
//                 .thenAnswer(invocation -> invocation.getArgument(0));
        
//         payHistory.addTransactionHistory(userTx);
//         payHistory.addTransactionHistory(storeTx);

//         // when
//         payService.refundForOrder(payId, storeEmail);

//         // then
//         verify(payHistoryRepository).findById(payId);
//         verify(transactionHistoryRepository).save(any(TransactionHistory.class));
        
//         assertEquals(PayType.REFUND, payHistory.getStatus());
//         assertEquals(TransactionStatus.REFUNDED, storeTx.getStatus());
//         assertEquals(new BigDecimal("1.0"), userAccount.getBalance()); // 환불된 금액 확인
//     }

//     @Test
//     @DisplayName("이미 환불된 결제에 대한 환불 시도 시 예외 발생")
//     void refundForOrder_AlreadyRefunded() {
//         // given
//         Long payId = 1L;
//         String storeEmail = "store@example.com";
        
//         PayHistory payHistory = PayHistory.builder()
//                 .id(payId)
//                 .status(PayType.REFUND) // 이미 환불된 상태
//                 .build();
        
//         when(payHistoryRepository.findById(payId)).thenReturn(Optional.of(payHistory));

//         // when & then
//         assertThrows(AlreadyRefundException.class, () -> payService.refundForOrder(payId, storeEmail));
//         verify(payHistoryRepository).findById(payId);
//         verify(transactionHistoryRepository, never()).save(any(TransactionHistory.class));
//     }

//     @Test
//     @DisplayName("이미 정산된 가맹점 거래에 대한 환불 시도 시 예외 발생")
//     void refundForOrder_StoreAlreadySettled() {
//         // given
//         Long payId = 1L;
//         String storeEmail = "store@example.com";
        
//         Account userAccount = createAccount(1L, "BTC", new BigDecimal("0.9975"));
//         Account storeAccount = createAccount(2L, "BTC", new BigDecimal("0.0025"));
        
//         PayHistory payHistory = PayHistory.builder()
//                 .id(payId)
//                 .status(PayType.APPROVED)
//                 .totalAmount(new BigDecimal("0.0025"))
//                 .build();
        
//         TransactionHistory userTx = TransactionHistory.builder()
//                 .id(1L)
//                 .account(userAccount)
//                 .payHistory(payHistory)
//                 .amount(new BigDecimal("-0.0025"))
//                 .status(TransactionStatus.ACCEPTED)
//                 .type(TransactionType.PAY)
//                 .build();
        
//         TransactionHistory storeTx = TransactionHistory.builder()
//                 .id(2L)
//                 .account(storeAccount)
//                 .payHistory(payHistory)
//                 .amount(new BigDecimal("0.0025"))
//                 .status(TransactionStatus.ACCEPTED) // 이미 정산된 상태
//                 .type(TransactionType.PAY)
//                 .build();
        
//         when(payHistoryRepository.findById(payId)).thenReturn(Optional.of(payHistory));
        
//         payHistory.addTransactionHistory(userTx);
//         payHistory.addTransactionHistory(storeTx);

//         // when & then
//         assertThrows(StoreAlreadySettledException.class, () -> payService.refundForOrder(payId, storeEmail));
//         verify(payHistoryRepository).findById(payId);
//         verify(transactionHistoryRepository, never()).save(any(TransactionHistory.class));
//     }

//     // 헬퍼 메서드들
//     private Order createOrder(String userEmail, String storeEmail, String currency, BigDecimal exchangeRate, int totalPrice) {
//         Actor user = Actor.builder()
//                 .email(userEmail)
//                 .build(); 
        
//         Actor store = Actor.builder()
//                 .email(storeEmail)
//                 .build();

//         Order order = Order.builder()
//                 .user(user)
//                 .store(store)
//                 .currency(currency)
//                 .exchangeRate(exchangeRate)
//                 .totalPrice(totalPrice)
//                 .status(OrderStatus.WAITING)
//                 .type(OrderType.TAKE_OUT)
//                 .build();
        
//         return order;
//     }
    
//     private Account createAccount(Long id, String currency, BigDecimal balance) {
//         Coin coin = Coin.builder()
//                 .id(id)
//                 .currency(currency)
//                 .name("Test" + currency)
//                 .build();
        
//         Account account = Account.builder()
//                 .id(id)
//                 .coin(coin)
//                 .balance(balance)
//                 .availableBalance(balance)
//                 .build();
        
//         return account;
//     }
// }