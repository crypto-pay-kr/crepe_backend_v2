package dev.crepe.domain.core.subscribe.model.dto.response;

import dev.crepe.domain.channel.actor.store.model.StoreType;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SubscribeVoucherDto {
    private Long id;                    // 바우처 ID
    private String productName;        // 상품 이름
    private BigDecimal balance;        // 잔액
    private LocalDateTime expiredDate; // 만기일
    private StoreType storeType;          // 사용 가능한 가맹점 유형 (편의점, 카페 등)
    private String bankTokenSymbol;

    public static SubscribeVoucherDto from(Subscribe subscribe) {
        return  SubscribeVoucherDto.builder()
                .id(subscribe.getId())
                .productName(subscribe.getProduct().getProductName())
                .balance(subscribe.getBalance())
                .expiredDate(subscribe.getExpiredDate())
                .storeType(subscribe.getProduct().getStoreType())
                .bankTokenSymbol(subscribe.getProduct().getBankToken().getCurrency())
                .build();
    }
}
