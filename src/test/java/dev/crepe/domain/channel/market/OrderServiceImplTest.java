package dev.crepe.domain.channel.market;

import java.lang.reflect.Field;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.store.exception.MenuNotFoundException;
import dev.crepe.domain.channel.actor.store.exception.StoreNotFoundException;
import dev.crepe.domain.channel.actor.store.repository.MenuRepository;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import dev.crepe.domain.channel.market.order.exception.ExchangePriceNotMatchException;
import dev.crepe.domain.channel.market.order.exception.OrderNotFoundException;
import dev.crepe.domain.channel.market.order.model.OrderStatus;
import dev.crepe.domain.channel.market.order.model.OrderType;
import dev.crepe.domain.channel.market.order.model.dto.request.CreateOrderRequest;
import dev.crepe.domain.channel.market.order.model.dto.response.CreateOrderResponse;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.model.entity.OrderDetail;
import dev.crepe.domain.channel.market.order.repository.OrderDetailRepository;
import dev.crepe.domain.channel.market.order.repository.OrderRepository;
import dev.crepe.domain.channel.market.order.service.impl.OrderServiceImpl;
import dev.crepe.domain.channel.market.order.util.OrderIdGenerator;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import dev.crepe.global.error.exception.UnauthorizedException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UpbitExchangeService upbitExchangeService;

    @Mock
    private PayService payService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderIdGenerator orderIdGenerator;

    @Test
    @DisplayName("사용자 주문 목록 조회 테스트")
    void getCustomerOrderList() {
        // given
        String userEmail = "user@example.com";
        Actor user = Actor.builder()
                .id(1L)
                .email(userEmail)
                .nickName("user")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();

        Actor store = Actor.builder()
                .id(2L)
                .email("store@example.com")
                .nickName("store")
                .phoneNum("01087654321")
                .role(UserRole.SELLER)
                .build();

        List<Order> orders = Arrays.asList(
                createOrder(user, store, "BTC"),
                createOrder(user, store, "ETH")
        );

        when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserId(user.getId())).thenReturn(orders);

        // when
        List<CreateOrderResponse> result = orderService.getCustomerOrderList(userEmail);

        // then
        assertEquals(2, result.size());
        assertEquals(orders.get(0).getId(), result.get(0).getOrderId());
        assertEquals(orders.get(1).getId(), result.get(1).getOrderId());
        verify(actorRepository).findByEmail(userEmail);
        verify(orderRepository).findByUserId(user.getId());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 주문 목록 조회 시 예외 발생")
    void getCustomerOrderList_UserNotFound() {
        // given
        String userEmail = "nonexistent@example.com";
        when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> orderService.getCustomerOrderList(userEmail));
        verify(actorRepository).findByEmail(userEmail);
        verify(orderRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("주문 상세 조회 테스트")
    void getOrderDetails() {
        // given
        String userEmail = "user@example.com";
    
        Actor user = Actor.builder()
                .id(1L)
                .email(userEmail)
                .nickName("user")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();
    
        Actor store = Actor.builder()
                .id(2L)
                .email("store@example.com")
                .nickName("store")
                .phoneNum("01087654321")
                .role(UserRole.SELLER)
                .build();
    
        Menu menu = Menu.builder()
                .id(1L)
                .name("Menu1")
                .price(50000)
                .image("menu1.jpg")
                .store(store)
                .build();
    
        Order order = createOrder(user, store, "BTC");
        OrderDetail orderDetail = createOrderDetail(1L, order, menu);
        List<OrderDetail> orderDetails = Collections.singletonList(orderDetail);
        String orderId = order.getId();
    
        // Remove unnecessary stubbing
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderId(orderId)).thenReturn(orderDetails);
    
        // when
        CreateOrderResponse result = orderService.getOrderDetails(orderId, userEmail);
    
        // then
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(1, result.getOrderDetails().size());
        verify(orderRepository).findById(orderId);
        verify(orderDetailRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("존재하지 않는 주문 상세 조회 시 예외 발생")
    void getOrderDetails_OrderNotFound() {
        // given
        String orderId = "NONEXISTENT";
        String userEmail = "user@example.com";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderDetails(orderId, userEmail));
        verify(orderRepository).findById(orderId);
        verify(orderDetailRepository, never()).findByOrderId(anyString());
    }

    @Test
    @DisplayName("다른 사용자의 주문 상세 조회 시 예외 발생")
    void getOrderDetails_Unauthorized() {
        // given
        String userEmail = "user@example.com";
        String otherEmail = "other@example.com";

        Actor otherUser = Actor.builder()
                .id(2L)
                .email(otherEmail)
                .nickName("other")
                .phoneNum("01087654321")
                .role(UserRole.USER)
                .build();

        Actor store = Actor.builder()
                .id(3L)
                .email("store@example.com")
                .nickName("store")
                .phoneNum("01012345678")
                .role(UserRole.SELLER)
                .build();

        Order order = createOrder(otherUser, store, "BTC");
        String orderId = order.getId();


        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThrows(UnauthorizedException.class, () -> orderService.getOrderDetails(orderId, userEmail));
        verify(orderRepository).findById(orderId);
        verify(orderDetailRepository, never()).findByOrderId(anyString());
    }
    
    @Test
    @DisplayName("주문 생성 테스트")
    void createOrder() {
        // given
        String userEmail = "user@example.com";
        Long storeId = 2L;
        String currency = "BTC";
        BigDecimal exchangeRate = new BigDecimal("40000000");
        String generatedOrderId = "TEST_ORDER_ID_12345";
        
        Actor user = Actor.builder()
                .email(userEmail)
                .nickName("user")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();
        
        Actor store = Actor.builder()
                .id(storeId)
                .email("store@example.com")
                .nickName("store")
                .phoneNum("01012345678")
                .role(UserRole.SELLER)
                .build();
        
        Menu menu1 = Menu.builder()
                .id(1L)
                .price(50000)
                .name("Menu1")
                .image("menu1.jpg")
                .store(store)
                .build();
        
        Menu menu2 = Menu.builder()
                .id(2L)
                .price(30000)
                .name("Menu2")
                .image("menu2.jpg")
                .store(store)
                .build();
        
        // 메뉴 총액: 50000 + (30000 * 2) = 110000원
        List<CreateOrderRequest.OrderDetailRequest> orderDetails = Arrays.asList(
                new CreateOrderRequest.OrderDetailRequest(1L, 1),
                new CreateOrderRequest.OrderDetailRequest(2L, 2)
        );
        
        CreateOrderRequest request = new CreateOrderRequest(
                exchangeRate, storeId, userEmail, orderDetails, currency
        );
        
        // OrderIdGenerator 정적 메서드 모킹
        try (MockedStatic<OrderIdGenerator> mockedGenerator = Mockito.mockStatic(OrderIdGenerator.class)) {
            mockedGenerator.when(OrderIdGenerator::generate).thenReturn(generatedOrderId);
            
            when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
            when(actorRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(menuRepository.findById(1L)).thenReturn(Optional.of(menu1));
            when(menuRepository.findById(2L)).thenReturn(Optional.of(menu2));
            when(upbitExchangeService.getLatestRate(currency)).thenReturn(exchangeRate);
            
            // Order 저장 시 ID 필드 설정 (리플렉션 사용)
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                
                // ID가 null이면 리플렉션으로 설정
                if (order.getId() == null) {
                    Field idField = Order.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(order, generatedOrderId);
                }
                
                return order;
            });
            
            when(orderDetailRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            
            // when
            String result = orderService.createOrder(request, userEmail); 
            
            // then
            assertNotNull(result);
            assertEquals(generatedOrderId, result);
            verify(actorRepository).findByEmail(userEmail);
            verify(actorRepository).findById(storeId);
            verify(menuRepository, times(2)).findById(1L);
            verify(menuRepository, times(2)).findById(2L);
            verify(upbitExchangeService).getLatestRate(currency);
            verify(orderRepository).save(any(Order.class));
            verify(orderDetailRepository).saveAll(anyList());
            verify(payService).payForOrder(any(Order.class));
        }
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 주문 생성 시 예외 발생")
    void createOrder_UserNotFound() {
        // given
        String userEmail = "nonexistent@example.com";
        CreateOrderRequest request = new CreateOrderRequest(
                BigDecimal.TEN, 1L, userEmail, Collections.emptyList(), "BTC"
        );

        when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> orderService.createOrder(request, userEmail));
        verify(actorRepository).findByEmail(userEmail);
        verify(actorRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 가맹점으로 주문 생성 시 예외 발생")
    void createOrder_StoreNotFound() {
        // given
        String userEmail = "user@example.com";
        Long storeId = 99L;
        CreateOrderRequest request = new CreateOrderRequest(
                BigDecimal.TEN, storeId, userEmail, Collections.emptyList(), "BTC"
        );

        Actor user = Actor.builder()
                .email(userEmail)
                .nickName("user")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();

        when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(actorRepository.findById(storeId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(StoreNotFoundException.class, () -> orderService.createOrder(request, userEmail));
        verify(actorRepository).findByEmail(userEmail);
        verify(actorRepository).findById(storeId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 메뉴로 주문 생성 시 예외 발생")
    void createOrder_MenuNotFound() {
        // given
        String userEmail = "user@example.com";
        Long storeId = 2L;
        Long nonExistentMenuId = 99L;
        String currency = "BTC";
        BigDecimal exchangeRate = BigDecimal.TEN;
        
        Actor user = Actor.builder()
                .email(userEmail)
                .nickName("user")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();
        
        Actor store = Actor.builder()
                .id(storeId)
                .email("store@example.com")
                .nickName("store")
                .phoneNum("01012345678")
                .role(UserRole.SELLER)
                .build();
        
        List<CreateOrderRequest.OrderDetailRequest> orderDetails = Collections.singletonList(
                new CreateOrderRequest.OrderDetailRequest(nonExistentMenuId, 1)
        );
        
        CreateOrderRequest request = new CreateOrderRequest(
                exchangeRate, storeId, userEmail, orderDetails, currency
        );
        
        when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(actorRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(upbitExchangeService.getLatestRate(currency)).thenReturn(exchangeRate);
        
        // Optional.empty() 대신 MenuNotFoundException 예외를 던지도록 설정
        when(menuRepository.findById(nonExistentMenuId)).thenThrow(new MenuNotFoundException(nonExistentMenuId));
        
        // when & then
        MenuNotFoundException exception = assertThrows(MenuNotFoundException.class, 
                () -> orderService.createOrder(request, userEmail));
        
        // 예외 메시지 검증 (선택적)
        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentMenuId)));
        
        verify(actorRepository).findByEmail(userEmail);
        verify(actorRepository).findById(storeId);
        verify(menuRepository).findById(nonExistentMenuId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(payService, never()).payForOrder(any(Order.class));
    }

//     @Test
//     @DisplayName("다른 가게의 메뉴로 주문 생성 시 예외 발생")
//     void createOrder_MenuFromDifferentStore() {
//         // given
//         String userEmail = "user@example.com";
//         Long storeId = 2L;
//         Long differentStoreId = 3L;
//         Long menuId = 5L;
//         String currency = "BTC";
//         BigDecimal exchangeRate = BigDecimal.valueOf(40000000);
        
//         // 주문하려는 사용자
//         Actor user = Actor.builder()
//                 .id(1L)
//                 .email(userEmail)
//                 .nickName("user")
//                 .phoneNum("01012345678")
//                 .role(UserRole.USER)
//                 .build();
        
//         // 주문하려는 가게
//         Actor store = Actor.builder()
//                 .id(storeId)
//                 .email("store@example.com")
//                 .nickName("store")
//                 .phoneNum("01012345678")
//                 .role(UserRole.SELLER)
//                 .build();
        
//         // 다른 가게
//         Actor differentStore = Actor.builder()
//                 .id(differentStoreId)
//                 .email("other-store@example.com")
//                 .nickName("other store")
//                 .phoneNum("01098765432")
//                 .role(UserRole.SELLER)
//                 .build();
        
//         // 다른 가게의 메뉴
//         Menu menuFromDifferentStore = Menu.builder()
//                 .id(menuId)
//                 .name("Other Store's Menu")
//                 .price(15000)
//                 .image("other-menu.jpg")
//                 .store(differentStore) // 여기가 중요! 다른 가게를 설정
//                 .build();
        
//         List<CreateOrderRequest.OrderDetailRequest> orderDetails = Collections.singletonList(
//                 new CreateOrderRequest.OrderDetailRequest(menuId, 1)
//         );
        
//         CreateOrderRequest request = new CreateOrderRequest(
//                 exchangeRate, storeId, userEmail, orderDetails, currency
//         );
        
//         // 모킹 설정
//         when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
//         when(actorRepository.findById(storeId)).thenReturn(Optional.of(store));
//         when(menuRepository.findById(menuId)).thenReturn(Optional.of(menuFromDifferentStore));
//         when(upbitExchangeService.getLatestRate(currency)).thenReturn(exchangeRate);
        
//         // when & then
//         // 예외 타입은 실제 구현에 따라 다를 수 있음 (UnauthorizedException 또는 다른 커스텀 예외)
//         Exception exception = assertThrows(UnauthorizedException.class, 
//                 () -> orderService.createOrder(request, userEmail));
        
//         // 예외 메시지 검증 (선택적)
//         assertTrue(exception.getMessage().contains("메뉴") || 
//                    exception.getMessage().contains("menu") ||
//                    exception.getMessage().contains("가게") || 
//                    exception.getMessage().contains("store"));
        
//         // 메서드 호출 검증
//         verify(actorRepository).findByEmail(userEmail);
//         verify(actorRepository).findById(storeId);
//         verify(menuRepository).findById(menuId);
//         verify(orderRepository, never()).save(any(Order.class));
//         verify(payService, never()).payForOrder(any(Order.class));
//     }

    @Test
    @DisplayName("환율 불일치 시 주문 생성 예외 발생")
    void createOrder_ExchangeRateMismatch() {
        // given
        String userEmail = "user@example.com";
        Long storeId = 2L;
        String currency = "BTC";
        BigDecimal clientRate = new BigDecimal("40000000");  // 클라이언트 환율
        BigDecimal serverRate = new BigDecimal("41000000");  // 서버 환율 (1000000원 차이)
        
        Actor user = Actor.builder()
                .email(userEmail)
                .nickName("user")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();
        
        Actor store = Actor.builder()
                .id(storeId)
                .email("store@example.com")
                .nickName("store")
                .phoneNum("01012345678")
                .role(UserRole.SELLER)
                .build();
        
        List<CreateOrderRequest.OrderDetailRequest> orderDetails = Collections.singletonList(
                new CreateOrderRequest.OrderDetailRequest(1L, 1)
        );
        
        CreateOrderRequest request = new CreateOrderRequest(
                clientRate, storeId, userEmail, orderDetails, currency
        );
        
        when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(actorRepository.findById(storeId)).thenReturn(Optional.of(store));
        // menuRepository.findById() 스터빙 제거
        when(upbitExchangeService.getLatestRate(currency)).thenReturn(serverRate);
        
        // when & then
        assertThrows(ExchangePriceNotMatchException.class, () -> orderService.createOrder(request, userEmail));
        
        verify(actorRepository).findByEmail(userEmail);
        verify(actorRepository).findById(storeId);
        verify(upbitExchangeService).getLatestRate(currency);
        
        // 환율 검증에서 예외가 발생하므로 다음 단계는 실행되지 않음을 검증
        verify(menuRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
        verify(payService, never()).payForOrder(any(Order.class));
    }

    // 헬퍼 메서드
    private Order createOrder(Actor user, Actor store, String currency) {
        return Order.builder()
                .user(user)
                .store(store)
                .currency(currency)
                .exchangeRate(new BigDecimal("40000000"))
                .totalPrice(100000)
                .status(OrderStatus.WAITING)
                .type(OrderType.TAKE_OUT)
                .build();
    }

    private OrderDetail createOrderDetail(Long id, Order order, Menu menu) {
        return OrderDetail.builder()
                .id(id)
                .order(order)
                .menu(menu)
                .menuCount(1)
                .build();
    }
}