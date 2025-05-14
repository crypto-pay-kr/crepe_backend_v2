package dev.crepe.domain.bank.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetBankInfoDetailResponse {
    private Long bankId;
    private String bankName;
    private String bankImageUrl;
    private String bankPhoneNumber;
    private String bankEmail;
    private String bankCode;

}
