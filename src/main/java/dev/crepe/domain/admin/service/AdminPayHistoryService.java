package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminPayHistoryService {



    Page<GetPayHistoryResponse> getPayHistoriesByUserId(Long userId, String type, Pageable pageable);

}
