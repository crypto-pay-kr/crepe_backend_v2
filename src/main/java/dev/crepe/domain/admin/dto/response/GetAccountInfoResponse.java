package dev.crepe.domain.admin.dto.response;

import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountInfoResponse {

    private String coinName;
    private String currency;
    private String address;
    private String tag;
    private String balance;
    private AddressRegistryStatus registryStatus;

}
