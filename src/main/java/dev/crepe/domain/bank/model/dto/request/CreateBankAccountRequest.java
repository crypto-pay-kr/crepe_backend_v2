package dev.crepe.domain.bank.model.dto.request;

import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankAccountRequest {
    private String bankName;
    private GetAddressRequest getAddressRequest;
}
