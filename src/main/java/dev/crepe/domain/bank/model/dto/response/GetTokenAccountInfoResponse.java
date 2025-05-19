package dev.crepe.domain.bank.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetTokenAccountInfoResponse {

    private String bankName;
    private String tokenName;
    private String tokenCurrency;
    private String balance;
    private String nonAvailableBalance;
    private String accountAddress;
}