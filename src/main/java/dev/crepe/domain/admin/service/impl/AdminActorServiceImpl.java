package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.dto.response.GetActorInfoResponse;
import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.admin.service.AdminActorService;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.account.model.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminActorServiceImpl implements AdminActorService {

    private final ActorRepository actorRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<GetActorInfoResponse> getActorsByRole(String role, int page, int size) {
        UserRole enumRole;
        try {
            enumRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 역할입니다: " + role);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return actorRepository.findByRole(enumRole, pageable)
                .map(actor -> GetActorInfoResponse.builder()
                        .actorId(actor.getId())
                        .actorName(actor.getName())
                        .actorEmail(actor.getEmail())
                        .actorPhoneNum(actor.getPhoneNum())
                        .actorRole(actor.getRole().name())
                        .actorStatus(actor.isDataStatus() ? "ACTIVE" : "INACTIVE")
                        .build());
    }




}
