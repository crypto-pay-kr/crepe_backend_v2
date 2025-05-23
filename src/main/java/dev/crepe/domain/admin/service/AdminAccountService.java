package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.response.GetAccountInfoResponse;
import dev.crepe.domain.admin.dto.response.GetAllTransactionHistoryResponse;
import org.springframework.data.domain.Page;



public interface AdminAccountService {

    Page<GetAccountInfoResponse> getAccountInfo(Long id,int page, int size);

    Page<GetAllTransactionHistoryResponse> getUserFullTransactionHistory(Long actorId, int page, int size);
}
