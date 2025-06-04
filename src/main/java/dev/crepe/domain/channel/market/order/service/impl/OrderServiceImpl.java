package dev.crepe.domain.channel.market.order.service.impl;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.store.repository.MenuRepository;
import dev.crepe.domain.channel.actor.store.repository.StoreRepository;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import dev.crepe.domain.channel.market.order.model.OrderStatus;
import dev.crepe.domain.channel.market.order.model.OrderType;
import dev.crepe.domain.channel.market.order.model.dto.request.CreateOrderRequest;
import dev.crepe.domain.channel.market.order.model.dto.response.CreateOrderResponse;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.model.entity.OrderDetail;
import dev.crepe.domain.channel.market.order.repository.OrderDetailRepository;
import dev.crepe.domain.channel.market.order.repository.OrderRepository;
import dev.crepe.domain.channel.market.order.service.OrderService;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.pay.PaymentType;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import dev.crepe.global.error.exception.ExceptionDbService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MenuRepository menuRepository;
    private final ActorRepository actorRepository;
    private final AccountRepository accountRepository;
    private final StoreRepository storeRepository;
    private final UpbitExchangeService upbitExchangeService;
    private final PayService payService;
    private final ExceptionDbService exceptionDbService;


//******************************************** 주문 내역 조회 start ******************************************/

    @Override
    @Transactional(readOnly = true)
    public List<CreateOrderResponse> getCustomerOrderList(String userEmail) {
        log.info("사용자 이메일로 주문 목록 조회: {}", userEmail);

        Actor user = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        List<Order> ordersList = orderRepository.findByUserId(user.getId());

        return ordersList.stream()
                .map(CreateOrderResponse::from)
                .collect(Collectors.toList());
    }


//******************************************** 주문 내역 조회 end ********************************************/


//******************************************** 주문 상세 내역 조회 start **************************************/

    @Override
    @Transactional(readOnly = true)
    public CreateOrderResponse getOrderDetails(String orderId, String userEmail) {

        log.info("사용자 이메일로 주문 상세 조회: {}", userEmail);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> exceptionDbService.getException("ORDER_002"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw exceptionDbService.getException("ACTOR_001");
        }

        // orderId로 OrderDetail 조회
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        if (orderDetails.size() != 1) {
            throw exceptionDbService.getException("ORDER_003");
        }

        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getStatus().name())
                .orderType(order.getType().name())
                .orderDetails(orderDetails.stream()
                        .map(CreateOrderResponse.OrderDetailResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }


//******************************************** 주문 상세 내역 조회 end ******************************************/


//******************************************** 주문 생성 start ************************************************/


    @Override
    @Transactional
    public String createOrder(CreateOrderRequest request, String userEmail) {

        log.info("주문 생성 시작 - 사용자 이메일: {}, 요청 정보: {}", userEmail, request);
        Actor user = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        Actor store = actorRepository.findById(request.getStoreId())
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        System.out.println("가맹점의 이메일 " + store.getEmail());

        // 결제 타입에 따라 필수값 체크
        PaymentType paymentType = request.getPaymentType();
        log.info("요청된 결제 타입: {}", paymentType);


        // 결제 타입에 따라 OrderRequest 분기
        switch (paymentType) {
            case COIN -> {
                if (request.getCurrency() == null || request.getExchangeRate() == null) {
                    throw exceptionDbService.getException("ORDER_006");
                }
                upbitExchangeService.validateRateWithinThreshold(
                        request.getExchangeRate(),
                        request.getCurrency(),
                        BigDecimal.valueOf(1)
                );
            }
            case VOUCHER -> {
                if (request.getVoucherSubscribeId() == null) {
                    throw exceptionDbService.getException("ORDER_007");
                }
            }
            default -> throw exceptionDbService.getException("ORDER_008");
        }


        Map<Long, Menu> menuMap = request.getOrderDetails().stream()
                .map(detail -> menuRepository.findById(detail.getMenuId())
                        .orElseThrow(() -> exceptionDbService.getException("MENU_001")))
                .collect(Collectors.toMap(Menu::getId, menu -> menu));


        int totalPrice = request.getOrderDetails().stream()
                .mapToInt(detail -> menuMap.get(detail.getMenuId()).getPrice() * detail.getMenuCount())
                .sum();

        log.info("총 주문 금액 계산 완료: {}", totalPrice);

        Order orders = Order.builder()
                .totalPrice(totalPrice)
                .status(OrderStatus.WAITING)
                .type(OrderType.TAKE_OUT)
                .currency(request.getCurrency())
                .exchangeRate(request.getExchangeRate())
                .user(user)
                .store(store)
                .build();

        orderRepository.save(orders);

        List<OrderDetail> orderDetails = request.getOrderDetails().stream()
                .map(detail -> OrderDetail.builder()
                        .menuCount(detail.getMenuCount())
                        .order(orders)
                        .menu(menuMap.get(detail.getMenuId()))
                        .build())
                .collect(Collectors.toList());

        orderDetailRepository.saveAll(orderDetails);
        log.info("주문 상세 저장 완료 - 주문 ID: {}", orders.getId());


        // 결제 처리
        switch (paymentType) {
            case VOUCHER -> payService.payWithVoucher(orders, request.getVoucherSubscribeId());
            case COIN -> payService.payForOrder(orders);
        }

        log.info("결제 처리 완료 - 주문 ID: {}", orders.getId());

        return orders.getId();

    }


//******************************************** 주문 생성 end **********************************************/


    @Override
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Order> getOrdersByStoreId(Long storeId, Pageable pageable) {
        return orderRepository.findByStoreId(storeId, pageable);
    }

    @Override
    public Page<Order> getOrdersByUserEmail(String userEmail, Pageable pageable) {
        return orderRepository.findByUserEmail(userEmail, pageable);

    }


    @Override
    public List<String> getAvailableCurrency(String userEmail, Long storeId) {
        log.info("주문 가능한 결제 수단 조회 시작 - 주문자 이메일: {}", userEmail);
        // 1. 사용자 계좌 조회
        List<Account> userAccounts = accountRepository.findByActor_EmailAndAddressRegistryStatus(
                userEmail, AddressRegistryStatus.ACTIVE);

        // 2. 가게 계좌 조회
        List<Coin> storeCoins = storeRepository.findById(storeId)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"))
                .getCoinList();

        List<Long> storeCoinIds = storeCoins.stream()
                .map(Coin::getId)
                .collect(Collectors.toList());

        List<Account> storeAccounts = accountRepository.findByActor_IdAndCoin_IdInAndAddressRegistryStatus(
                storeId, storeCoinIds, AddressRegistryStatus.ACTIVE);

        // 3. 가게의 bankToken 계좌 조회
        List<Account> storeBankTokenAccounts = accountRepository.findByStoreIdAndBankTokenIsNotNull(storeId);

        // 4. 사용자와 가게 계좌의 공통 Coin ID를 기준으로 Currency 반환
        Set<Long> activeUserCoinIds = userAccounts.stream()
                .filter(account -> account.getCoin() != null)
                .map(account -> account.getCoin().getId())
                .collect(Collectors.toSet());

        List<String> currencies = storeAccounts.stream()
                .filter(account -> account.getCoin() != null && activeUserCoinIds.contains(account.getCoin().getId()))
                .map(account -> account.getCoin().getCurrency())
                .distinct()
                .collect(Collectors.toList());

        // 5. 가게의 bankToken 계좌의 Currency 추가
        List<String> bankTokenCurrencies = storeBankTokenAccounts.stream()
                .map(account -> account.getBankToken().getCurrency())
                .distinct()
                .collect(Collectors.toList());

        currencies.addAll(bankTokenCurrencies);
        log.info("주문 가능한 결제수단 반환 완료 - 반환된 심볼 수: {}", currencies.size());
        return currencies.stream().distinct().collect(Collectors.toList());
    }
}