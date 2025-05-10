package dev.crepe.domain.channel.actor.store.service.impl;

import dev.crepe.domain.channel.actor.store.model.dto.request.StoreOrderActionRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderManageResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderResponse;
import dev.crepe.domain.channel.actor.store.service.StoreOrderService;
import dev.crepe.domain.channel.market.order.exception.InvalidOrderIdException;
import dev.crepe.domain.channel.market.order.model.OrderStatus;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.repository.OrderRepository;
import dev.crepe.domain.core.pay.exception.PayHistoryNotFoundException;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreOrderServiceImpl implements StoreOrderService {

    private  final OrderRepository orderRepository;
    private final PayHistoryRepository payHistoryRepository;
    private final PayService payService;


    //******************************************** 가맹점 주문 조회 start ********************************************/
    @Transactional(readOnly = true)
    public List<StoreOrderResponse> getAllList(Long storeId) {
        return orderRepository.findByStoreId(storeId).stream()
                .map(Order::toStoreOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoreOrderResponse> getWaitingList(Long storeId) {
        return orderRepository.findByStoreIdAndStatus(storeId, OrderStatus.WAITING).stream()
                .map(Order::toStoreOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoreOrderResponse> getProcessingList(Long storeId) {
        return orderRepository.findByStoreIdAndStatus(storeId, OrderStatus.PAID).stream()
                .map(Order::toStoreOrderResponse)
                .toList();
    }


    //******************************************** 가맹점 주문 조회 end ********************************************/




    //******************************************** 가맹점 주문 수락 start ********************************************/
    @Transactional
    public StoreOrderManageResponse acceptOrder(String orderId, Long storeId, StoreOrderActionRequest request) {
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(InvalidOrderIdException::new);

        if(order.getStatus() != OrderStatus.WAITING) {
            return StoreOrderManageResponse.builder()
                    .orderId(orderId)
                    .status(order.getStatus())
                    .message("이미 처리된 주문입니다.")
                    .build();
        }

        // 주문 상태 업데이트
        order.accept();
        orderRepository.save(order);

        //결제 상태 업데이트
        PayHistory payHistory = payHistoryRepository.findByOrder(order)
                .orElseThrow(PayHistoryNotFoundException::new);
        payHistory.approve();
        payHistoryRepository.save(payHistory);

        return StoreOrderManageResponse.builder()
                .orderId(orderId)
                .status(order.getStatus())
                .message(request.getPreparationTime().getDescription() + " 후 준비 예정입니다")
                .build();
    }


    //******************************************** 가맹점 주문 수락 end ********************************************/




    //******************************************** 가맹점 주문 거절 start ********************************************/
    @Transactional
    public StoreOrderManageResponse refuseOrder(String orderId, Long storeId, StoreOrderActionRequest request) {
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(InvalidOrderIdException::new);

        if(order.getStatus() != OrderStatus.WAITING) {
            return StoreOrderManageResponse.builder()
                    .orderId(orderId)
                    .status(order.getStatus())
                    .message("이미 처리된 주문입니다.")
                    .build();
        }
        //주문 상태 업데이트
        order.refuse();
        orderRepository.save(order);


        //결제 상태 업데이트
        payService.cancelForOrder(order);

        return StoreOrderManageResponse.builder()
                .orderId(orderId)
                .status(order.getStatus())
                .message("주문이 거절되었습니다. 사유 : " + request.getRefusalReason().getDescription())
                .build();
    }


    //******************************************** 가맹점 주문 거절 end ********************************************/




    //******************************************** 주문 프로세스 종료 start ********************************************/

    @Transactional
    public StoreOrderManageResponse completeOrder(String orderId, Long storeId) {
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(InvalidOrderIdException::new);


        // 주문 상태 확인
        if (order.getStatus() != OrderStatus.PAID) {
            return StoreOrderManageResponse.builder()
                    .orderId(orderId)
                    .status(order.getStatus())
                    .message("완료할 수 없는 주문 상태입니다.")
                    .build();
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
