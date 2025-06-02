package dev.crepe.domain.admin.service.impl;


import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.admin.service.AdminAddressService;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.global.error.exception.ExceptionDbService;
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
    private final AccountService accountService;
    private final ExceptionDbService exceptionDbService;
    // 계좌 등록 요청 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<GetPendingWithdrawAddressListResponse> getPendingAddressList(
            int page,
            int size,
            List<AddressRegistryStatus> statuses,
            Boolean isBankAccount
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Account> accounts;

        if (isBankAccount) {
            accounts = accountRepository.findByActorIsNullAndAddressRegistryStatusInAndCoinIsNotNull(statuses, pageable);
        } else {
            accounts = accountRepository.findByActorIsNotNullAndAddressRegistryStatusInAndCoinIsNotNull(statuses, pageable);
        }

        return accounts.map(account -> GetPendingWithdrawAddressListResponse.builder()
                .id(account.getId())
                .depositor(
                        account.getActor() == null
                                ? account.getBank().getName()
                                : account.getActor().getName()
                )
                .userType(isBankAccount ? "BANK" : account.getActor().getName())
                .currency(account.getCoin().getCurrency())
                .address(account.getAccountAddress())
                .tag(account.getTag())
                .addressRegistryStatus(account.getAddressRegistryStatus().name())
                .createdAt(account.getUpdatedAt())
                .build()
        );
    }

    //계좌 승인
    @Override
    @Transactional
    public String approveAddress(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));

        if (account.getAddressRegistryStatus() == AddressRegistryStatus.ACTIVE) {
            throw exceptionDbService.getException("ACCOUNT_002");
        }
        account.approveAddress();
        return  account.getAccountAddress();
    }

    //계좌 거절
    @Override
    @Transactional
    public void rejectAddress(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));

        if (account.getAddressRegistryStatus() == AddressRegistryStatus.REJECTED) {
            throw exceptionDbService.getException("ADDRESS_004");
        }
        account.adminRejectAddress();
    }

    // 계좌 해지 요청 승인
    @Override
    @Transactional
    public void unRegisterAddress(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));

        if (account.getAddressRegistryStatus() == AddressRegistryStatus.NOT_REGISTERED) {
            throw exceptionDbService.getException("ADDRESS_003");
        }

        account.adminUnRegisterAddress();
    }


    // 일반 유저 계좌 정지 (코인 + 토큰)
    @Override
    public void holdActorAddress(Long accountId) {
        Account account = accountService.getAccountById(accountId);

        accountService.holdAccount(account);

    }
}
