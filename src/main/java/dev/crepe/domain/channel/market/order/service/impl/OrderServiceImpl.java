package dev.crepe.domain.channel.market.order.service.impl;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.store.exception.MenuNotFoundException;
import dev.crepe.domain.channel.actor.store.exception.StoreNotFoundException;
import dev.crepe.domain.channel.actor.store.repository.MenuRepository;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
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
import dev.crepe.global.error.exception.NotSingleObjectException;
import dev.crepe.global.error.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MenuRepository menuRepository;
    private final ActorRepository actorRepository;



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

        Actor store = actorRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException(request.getStoreId()));

        Order orders = Order.builder()
                .totalPrice(calculateTotalPrice(request))
                .status(OrderStatus.WAITING)
                .type(OrderType.TAKE_OUT)
                .currency(request.getCurrency())
                .user(user)
                .store(store)
                .build();

        orderRepository.save(orders);

        List<OrderDetail> orderDetails = request.getOrderDetails().stream()
                .map(detail -> OrderDetail.builder()
                        .menuCount(detail.getMenuCount())
                        .order(orders)
                        .menu(menuRepository.findById(detail.getMenuId())
                                .orElseThrow(() -> new MenuNotFoundException(detail.getMenuId())))
                        .build())
                .collect(Collectors.toList());

        orderDetailRepository.saveAll(orderDetails);

        return orders.getId();
    }


//******************************************** 주문 생성 end **********************************************/



    private int calculateTotalPrice(CreateOrderRequest request) {
        return request.getOrderDetails().stream()
                .mapToInt(detail -> menuRepository.findById(detail.getMenuId())
                        .orElseThrow(() -> new MenuNotFoundException(detail.getMenuId()))
                        .getPrice() * detail.getMenuCount())
                .sum();
    }
}