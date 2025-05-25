package dev.crepe.domain.core.product.model.dto.response;

import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.BankProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetOnsaleProductListReponse {

    private Long id;
    private BankProductType type;
    private String productName;      // 상품 이름
    private String bankName;         // 주관은행
    private BigDecimal totalBudget; // 혜택 자본금
    private BigDecimal remainingBudget; // 남은 자본금 수량
    private Integer totalParticipants;
    private Integer currentParticipants; // 현재 참여자 수
    private BankProductStatus status;
    private Float minInterestRate;
    private Float maxInterestRate;
    private String imageUrl;
    private List<String> tags;
    private String guideFile;
    private String deadline; // 마감 기한 (ISO 8601 형식의 문자열로 가정)


}
