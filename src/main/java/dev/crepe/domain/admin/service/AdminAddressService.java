package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import org.springframework.data.domain.Page;


public interface AdminAddressService {

    Page<GetPendingWithdrawAddressListResponse> getPendingAddressList(int page, int size, AddressRegistryStatus status);
    String approveAddress(Long accountId);
}
