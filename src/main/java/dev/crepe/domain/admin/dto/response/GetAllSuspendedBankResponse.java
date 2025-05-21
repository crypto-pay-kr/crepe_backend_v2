package dev.crepe.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllSuspendedBankResponse {
    private Long id;
    private String name;
    private LocalDate suspendedDate;
    private String bankPhoneNum;
    private BigDecimal totalSupply;
}
