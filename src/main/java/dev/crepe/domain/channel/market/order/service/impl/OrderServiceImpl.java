package dev.crepe.domain.channel.market.order.service.impl;


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
import dev.crepe.domain.channel.market.order.service.OrderService;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import dev.crepe.global.error.exception.NotSingleObjectException;
import dev.crepe.global.error.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MenuRepository menuRepository;
    private final ActorRepository actorRepository;
    private final UpbitExchangeService upbitExchangeService;
    private final PayService payService;



//******************************************** 주문 내역 조회 start ******************************************/

    @Override
    @Transactional(readOnly = true)
    public List<CreateOrderResponse> getCustomerOrderList(String userEmail) {

        Actor user= actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("해당 주문을 조회할 권한이 없습니다.");
        }

        // orderId로 OrderDetail 조회
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        if (orderDetails.size() != 1) {
            throw new NotSingleObjectException();
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

        Actor user = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        System.out.println("주문자의 이메일 " + request.getUserEmail());

        Actor store = actorRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException(request.getStoreId()));

        System.out.println("가맹점의 이메일 " + store.getEmail());

        upbitExchangeService.validateRateWithinThreshold(request.getExchangeRate(),request.getCurrency(),BigDecimal.valueOf(1));

        Map<Long, Menu> menuMap = request.getOrderDetails().stream()
                .map(detail -> menuRepository.findById(detail.getMenuId())
                        .orElseThrow(() -> new MenuNotFoundException(detail.getMenuId())))
                .collect(Collectors.toMap(Menu::getId, menu -> menu));


        int totalPrice = request.getOrderDetails().stream()
                .mapToInt(detail -> menuMap.get(detail.getMenuId()).getPrice() * detail.getMenuCount())
                .sum();


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

        // 결제 내역 등록
        payService.payForOrder(orders);

        return orders.getId();

    }


//******************************************** 주문 생성 end **********************************************/


    @Override
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId,pageable);
    }

    @Override
    public Page<Order> getOrdersByStoreId(Long storeId, Pageable pageable) {
        return orderRepository.findByStoreId(storeId, pageable);
    }

    @Override
    public Page<Order> getOrdersByUserEmail(String userEmail, Pageable pageable) {
        return orderRepository.findByUserEmail(userEmail,pageable);

    }
}