package dev.crepe.domain.admin.service.impl;


import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.admin.exception.AlreadyApprovedAddressException;
import dev.crepe.domain.admin.service.AdminAddressService;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAddressServiceImpl implements AdminAddressService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<GetPendingWithdrawAddressListResponse> getPendingAddressList(int page, int size, AddressRegistryStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Account> accounts = accountRepository.findByAddressRegistryStatus(status, pageable);

        return accounts.map(account -> GetPendingWithdrawAddressListResponse.builder()
                .id(account.getId())
                .storeName(account.getActor().getName())
                .currency(account.getCoin().getCurrency())
                .address(account.getAccountAddress())
                .tag(account.getTag())
                .addressRegistryStatus(account.getAddressRegistryStatus().name())
                .build());
    }
    @Override
    @Transactional
    public String approveAddress(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException());

        if (account.getAddressRegistryStatus() == AddressRegistryStatus.ACTIVE) {
            throw new AlreadyApprovedAddressException(account.getAccountAddress());
        }
        account.approveAddress();
        return  account.getAccountAddress();
    }


}
