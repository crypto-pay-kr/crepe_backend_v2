

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
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
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
    private ExceptionDbService exceptionDbService;

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
        when(exceptionDbService.getException("ACTOR_002"))
                .thenReturn(new CustomException("ACTOR_002", null, "사용자를 찾을 수 없습니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> orderService.getCustomerOrderList(userEmail));
        assertEquals("ACTOR_002", exception.getCode());
        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());

        verify(actorRepository).findByEmail(userEmail);
        verify(exceptionDbService).getException("ACTOR_002");
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
        when(exceptionDbService.getException("ORDER_002"))
                .thenReturn(new CustomException("ORDER_002", null, "주문을 찾을 수 없습니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> orderService.getOrderDetails(orderId, userEmail));
        assertEquals("ORDER_002", exception.getCode());
        assertEquals("주문을 찾을 수 없습니다.", exception.getMessage());

        verify(orderRepository).findById(orderId);
        verify(exceptionDbService).getException("ORDER_002");
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
        doThrow(new CustomException("ACTOR_001", null, "권한이 없는 사용자입니다."))
                .when(exceptionDbService).throwExceptionAndReturn("ACTOR_001");

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> orderService.getOrderDetails(orderId, userEmail));
        assertEquals("ACTOR_001", exception.getCode());
        assertEquals("권한이 없는 사용자입니다.", exception.getMessage());

        verify(orderRepository).findById(orderId);
        verify(exceptionDbService).throwExceptionAndReturn("ACTOR_001");
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

        List<CreateOrderRequest.OrderDetailRequest> orderDetails = Arrays.asList(
                new CreateOrderRequest.OrderDetailRequest(1L, 1),
                new CreateOrderRequest.OrderDetailRequest(2L, 2)
        );

        CreateOrderRequest request = new CreateOrderRequest(
                exchangeRate, storeId, userEmail, orderDetails, currency
        );

        try (MockedStatic<OrderIdGenerator> mockedGenerator = Mockito.mockStatic(OrderIdGenerator.class)) {
            mockedGenerator.when(OrderIdGenerator::generate).thenReturn(generatedOrderId);

            when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
            when(actorRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(menuRepository.findById(1L)).thenReturn(Optional.of(menu1));
            when(menuRepository.findById(2L)).thenReturn(Optional.of(menu2));

            // validateRateWithinThreshold 호출을 검증하도록 수정
            doNothing().when(upbitExchangeService).validateRateWithinThreshold(exchangeRate, currency, BigDecimal.valueOf(1));

            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                Field idField = Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(order, generatedOrderId);
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
            verify(menuRepository).findById(1L);
            verify(menuRepository).findById(2L);
            verify(upbitExchangeService).validateRateWithinThreshold(exchangeRate, currency, BigDecimal.valueOf(1));
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
        when(exceptionDbService.getException("ACTOR_002"))
                .thenReturn(new CustomException("ACTOR_002", null, "존재하지 않는 사용자입니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> orderService.createOrder(request, userEmail));
        assertEquals("ACTOR_002", exception.getCode());
        assertEquals("존재하지 않는 사용자입니다.", exception.getMessage());

        verify(actorRepository).findByEmail(userEmail);
        verify(exceptionDbService).getException("ACTOR_002");
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
        when(exceptionDbService.throwExceptionAndReturn("STORE_001"))
                .thenThrow(new CustomException("STORE_001", null, "가게 정보를 찾을 수 없습니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> orderService.createOrder(request, userEmail));
        assertEquals("STORE_001", exception.getCode());
        assertEquals("가게 정보를 찾을 수 없습니다.", exception.getMessage());


        verify(actorRepository).findByEmail(userEmail);
        verify(actorRepository).findById(storeId);
        verify(exceptionDbService).throwExceptionAndReturn("STORE_001");
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
        when(menuRepository.findById(nonExistentMenuId))
                .thenThrow(new CustomException("MENU_001", null, "메뉴 정보를 찾을 수 없습니다."));


        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> orderService.createOrder(request, userEmail));
        assertEquals("MENU_001", exception.getCode());
        assertEquals("메뉴 정보를 찾을 수 없습니다.", exception.getMessage());

        verify(actorRepository).findByEmail(userEmail);
        verify(actorRepository).findById(storeId);
        verify(menuRepository).findById(nonExistentMenuId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(payService, never()).payForOrder(any(Order.class));
    }


    @Test
    @DisplayName("환율 불일치 시 주문 생성 예외 발생")
    void createOrder_ExchangeRateMismatch() {
        // given
        String userEmail = "user@example.com";
        Long storeId = 2L;
        String currency = "BTC";
        BigDecimal clientRate = new BigDecimal("40000000");

        Actor user = Actor.builder()
                .email(userEmail)
                .role(UserRole.USER)
                .build();
        Actor store = Actor.builder()
                .id(storeId)
                .role(UserRole.SELLER)
                .build();

        CreateOrderRequest request = new CreateOrderRequest(
                clientRate, storeId, userEmail,
                Collections.singletonList(new CreateOrderRequest.OrderDetailRequest(1L, 1)),
                currency
        );

        when(actorRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(actorRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(exceptionDbService.getException("EXCHANHGE_002"))
                .thenReturn(new CustomException("EXCHANHGE_002", null, "시세가 유효하지 않습니다."));

        // validateRateWithinThreshold 호출 시 예외를 던지도록 설정
        doThrow(exceptionDbService.getException("EXCHANHGE_002"))
                .when(upbitExchangeService)
                .validateRateWithinThreshold(clientRate, currency, BigDecimal.valueOf(1));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> orderService.createOrder(request, userEmail));
        assertEquals("EXCHANHGE_002", exception.getCode());
        assertEquals("시세가 유효하지 않습니다.", exception.getMessage());

        verify(actorRepository).findByEmail(userEmail);
        verify(actorRepository).findById(storeId);
        verify(upbitExchangeService).validateRateWithinThreshold(clientRate, currency, BigDecimal.valueOf(1));

        // 예외가 발생하므로 아래 호출은 없어야 함
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