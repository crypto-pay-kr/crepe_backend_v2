package dev.crepe.domain.channel.actor.store.service.impl;

import dev.crepe.domain.channel.actor.store.EnumValidationService;
import dev.crepe.domain.channel.actor.store.model.dto.request.StoreOrderActionRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderManageResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderResponse;
import dev.crepe.domain.channel.actor.store.service.StoreOrderService;
import dev.crepe.domain.channel.market.order.model.OrderStatus;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.repository.OrderRepository;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreOrderServiceImpl implements StoreOrderService {

    private final OrderRepository orderRepository;
    private final PayHistoryRepository payHistoryRepository;
    private final PayService payService;
    private final ExceptionDbService exceptionDbService;
    private final EnumValidationService enumValidationService;
    private final RedisTemplate<String, String> redisTemplate;


    //******************************************** 가맹점 주문 조회 start ********************************************/
    @Transactional(readOnly = true)
    public List<StoreOrderResponse> getAllList(Long storeId) {
        return orderRepository.findByStoreId(storeId).stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .map(order -> {
                    String clientOrderNumber = redisTemplate.opsForValue().get("order_number:" + storeId + ":" + order.getId());
                    return StoreOrderResponse.builder()
                            .orderId(order.getId())
                            .clientOrderNumber(clientOrderNumber) // Redis에서 조회한 값 추가
                            .totalPrice(order.getTotalPrice())
                            .status(order.getStatus())
                            .orderType(order.getType().name())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .readyAt(order.getReadyAt())
                            .orderDetails(order.getOrderDetails().stream()
                                    .map(detail -> StoreOrderResponse.OrderDetailResponse.builder()
                                            .menuName(detail.getMenu().getName())
                                            .menuCount(detail.getMenuCount())
                                            .menuPrice(detail.getMenu().getPrice())
                                            .build())
                                    .toList())
                            .build();
                })
                .toList();
    }

    //******************************************** 가맹점 주문 조회 end ********************************************/


    //******************************************** 가맹점 주문 수락 start ********************************************/
    @Transactional
    public StoreOrderManageResponse acceptOrder(String orderId, Long storeId, StoreOrderActionRequest request) {
        log.info("주문 수락 요청 - 주문 ID: {}, 가게 ID: {}", orderId, storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> exceptionDbService.getException("ORDER_003"));

        int isStatusWaintingOrCompleted = (order.getStatus() == OrderStatus.WAITING || order.getStatus() == OrderStatus.COMPLETED) ?
                1 : 0;
        if (isStatusWaintingOrCompleted == 0) {
            return StoreOrderManageResponse.builder()
                    .orderId(orderId)
                    .status(order.getStatus())
                    .message("이미 처리된 주문입니다.")
                    .build();
        }

        enumValidationService.validatePreparationTime(request.getPreparationTime());

        // 주문 상태 업데이트
        order.accept(request.getPreparationTime());
        orderRepository.save(order);

        //결제 상태 업데이트
        PayHistory payHistory = payHistoryRepository.findByOrder(order)
                .orElseThrow(() -> exceptionDbService.getException("PAY_HISTORY_001"));
        payHistory.approve();
        payHistoryRepository.save(payHistory);

        log.info("주문 수락 완료 - 주문 ID: {}, 상태: {}", orderId, order.getStatus());
        return StoreOrderManageResponse.builder()
                .orderId(orderId)
                .status(order.getStatus())
                .updatedAt(order.getUpdatedAt())
                .ReadyAt(order.getReadyAt())
                .message(request.getPreparationTime().getDescription() + " 후 준비 예정입니다")
                .build();
    }


    //******************************************** 가맹점 주문 수락 end ********************************************/


    //******************************************** 가맹점 주문 거절 start ********************************************/
    @Transactional
    public StoreOrderManageResponse refuseOrder(String orderId, Long storeId, StoreOrderActionRequest request) {
        log.info("주문 거절 요청 - 주문 ID: {}, 가게 ID: {}", orderId, storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> exceptionDbService.getException("ORDER_003"));

        if (order.getStatus() != OrderStatus.WAITING) {
            return StoreOrderManageResponse.builder()
                    .orderId(orderId)
                    .status(order.getStatus())
                    .message("이미 처리된 주문입니다.")
                    .build();
        }

        enumValidationService.validateRefusalReason(request.getRefusalReason());

        //주문 상태 업데이트
        order.refuse();
        orderRepository.save(order);


        //결제 상태 업데이트
        payService.cancelForOrder(order);

        log.info("주문 거절 완료 - 주문 ID: {}, 상태: {}", orderId, order.getStatus());
        return StoreOrderManageResponse.builder()
                .orderId(orderId)
                .status(order.getStatus())
                .message("주문이 거절되었습니다. 사유 : " + request.getRefusalReason().getDescription())
                .build();
    }


    //******************************************** 가맹점 주문 거절 end ********************************************/


    public StoreOrderManageResponse cancelOrder(String orderId, Long storeId) {
        log.info("주문 완료 요청 취소- 주문 ID: {}, 가게 ID: {}", orderId, storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> exceptionDbService.getException("ORDER_003"));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw exceptionDbService.getException("ORDER_004"); // 취소 불가능한 상태
        }

        order.cancel();
        orderRepository.save(order);

        log.info("주문 완료 요청 취소 완료 - 주문 ID: {}, 가게 ID: {}", orderId, storeId);
        return StoreOrderManageResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .message("주문 완료 요청이 취소되었습니다.")
                .build();
    }


    //******************************************** 주문 프로세스 종료 start ********************************************/

    @Transactional
    public StoreOrderManageResponse completeOrder(String orderId, Long storeId) {
        log.info("주문 완료 요청 - 주문 ID: {}, 가게 ID: {}", orderId, storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> exceptionDbService.getException("ORDER_003"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw exceptionDbService.getException("ORDER_005");
        }

        // 주문 완료 처리
        order.complete();
        orderRepository.save(order);

        return StoreOrderManageResponse.builder()
                .orderId(orderId)
                .status(order.getStatus())
                .message("주문이 종료되었습니다.")
                .build();
    }


    //******************************************** 주문 프로세스 종료 end ********************************************/


}
