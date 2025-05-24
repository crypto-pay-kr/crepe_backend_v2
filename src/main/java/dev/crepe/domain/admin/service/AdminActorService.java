package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.response.GetAccountInfoResponse;
import dev.crepe.domain.admin.dto.response.GetActorInfoResponse;
import org.springframework.data.domain.Page;


public interface AdminActorService {

    Page<GetActorInfoResponse> getActorsByRole(String role,int page, int size);
}
