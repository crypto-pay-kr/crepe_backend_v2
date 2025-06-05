 package dev.crepe.domain.core.pay;

 import dev.crepe.domain.channel.actor.model.entity.Actor;
 import dev.crepe.domain.channel.actor.store.model.StoreType;
 import dev.crepe.domain.channel.market.order.model.entity.Order;
 import dev.crepe.domain.core.account.model.entity.Account;
 import dev.crepe.domain.core.account.repository.AccountRepository;
 import dev.crepe.domain.core.pay.service.impl.PayServiceImpl;
 import dev.crepe.domain.core.product.model.BankProductType;
 import dev.crepe.domain.core.product.model.entity.Product;
 import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
 import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
 import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
 import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
 import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
 import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
 import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
 import dev.crepe.global.error.exception.CustomException;
 import dev.crepe.global.error.exception.ExceptionDbService;
 import org.junit.jupiter.api.DisplayName;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;

 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.Optional;

 import static org.junit.jupiter.api.Assertions.*;
 import static org.mockito.Mockito.*;

 @ExtendWith(MockitoExtension.class)
 class PayServiceImplTest {

     @Mock
     private AccountRepository accountRepository;

     @Mock
     private CoinRepository coinRepository;

     @Mock
     private PayHistoryRepository payHistoryRepository;

     @Mock
     private SubscribeRepository subscribeRepository;

     @InjectMocks
     private PayServiceImpl payService;

     @Mock
     private ExceptionDbService exceptionDbService;


     @Test
     @DisplayName("가맹점 계정 미존재 시 예외 코드 검증 테스트")
     void payForOrder_StoreAccountNotFound_ExceptionCode() {
         // given
         when(exceptionDbService.getException("ACCOUNT_002"))
                 .thenReturn(new CustomException("ACCOUNT_002", null, null));

         Order order = createOrder("user@example.com", "store@example.com", "BTC", new BigDecimal("40000000"), 100000);
         when(accountRepository.findByActor_EmailAndCoin_Currency(order.getUser().getEmail(), order.getCurrency()))
                 .thenReturn(Optional.of(createAccount(1L, "BTC", new BigDecimal("1.0"))));
         when(accountRepository.findByActor_EmailAndCoin_Currency(order.getStore().getEmail(), order.getCurrency()))
                 .thenReturn(Optional.empty());

         // when & then
         CustomException exception = assertThrows(CustomException.class, () -> payService.payForOrder(order));
         assertEquals("ACCOUNT_002", exception.getCode());
     }

     @Test
     @DisplayName("유효하지 않은 상품권으로 결제 시 예외 코드 검증 테스트")
     void payWithVoucher_InvalidVoucher_ExceptionCode() {
         // given
         when(exceptionDbService.getException("PAY_003"))
                 .thenReturn(new CustomException("PAY_003", null, null));

         Order order = createOrder("user@example.com", "store@example.com", "BTC", new BigDecimal("40000000"), 100000);
         Long invalidSubscribeId = 999L;

         when(subscribeRepository.findById(invalidSubscribeId))
                 .thenReturn(Optional.empty());

         // when & then
         CustomException exception = assertThrows(CustomException.class, () -> payService.payWithVoucher(order, invalidSubscribeId));
         assertEquals("PAY_003", exception.getCode());
     }

     @Test
     @DisplayName("결제 내역 미존재 시 주문 취소 예외 코드 검증 테스트")
     void cancelForOrder_PayHistoryNotFound_ExceptionCode() {
         // given
         when(exceptionDbService.getException("PAY_HISTORY_001"))
                 .thenReturn(new CustomException("PAY_HISTORY_001", null, null));

         Order order = createOrder("user@example.com", "store@example.com", "BTC", new BigDecimal("40000000"), 100000);
         when(payHistoryRepository.findByOrder(order))
                 .thenReturn(Optional.empty());

         when(coinRepository.findAllCurrencies()) // Mock 동작 설정
                 .thenReturn(Arrays.asList("BTC", "ETH"));

         // when & then
         CustomException exception = assertThrows(CustomException.class, () -> payService.cancelForOrder(order));
         assertEquals("PAY_HISTORY_001", exception.getCode());
     }

     // 헬퍼 메서드
     private Order createOrder(String userEmail, String storeEmail, String currency, BigDecimal exchangeRate, int totalPrice) {
         Actor user = Actor.builder().email(userEmail).build();
         Actor store = Actor.builder()
                 .email(storeEmail)
                 .storeType(StoreType.RESTAURANT)
                 .build();
         return Order.builder()
                 .user(user)
                 .store(store)
                 .currency(currency)
                 .exchangeRate(exchangeRate)
                 .totalPrice(totalPrice)
                 .build();
     }

     private Account createAccount(Long id, String currency, BigDecimal balance) {
         Coin coin = Coin.builder().id(id).currency(currency).build();
         return Account.builder()
                 .id(id)
                 .coin(coin)
                 .balance(balance)
                 .build();
     }
 }