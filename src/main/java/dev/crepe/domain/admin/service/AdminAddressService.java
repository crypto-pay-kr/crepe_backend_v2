package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.request.RejectAddressRequest;
import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import org.springframework.data.domain.Page;
import java.util.List;

public interface AdminAddressService {

    Page<GetPendingWithdrawAddressListResponse> getPendingAddressList(int page, int size, List<AddressRegistryStatus> status, Boolean isBankAccount);
    String approveAddress(Long accountId);
    void rejectAddress(Long accountId);
    void unRegisterAddress(Long accountId);
}
