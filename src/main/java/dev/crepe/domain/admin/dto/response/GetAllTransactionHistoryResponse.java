package dev.crepe.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllTransactionHistoryResponse {

    private String currency;
    private String TransferAt;
    private String TransactionId;
    private String TransactionStatus;
    private String TransactionType;
    private String Amount;


}
