package dev.crepe.domain.admin.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllBankResponse {
    private Long id;
    private String name;
    private String bankPhoneNum;
    private BigDecimal totalSupply;
}
