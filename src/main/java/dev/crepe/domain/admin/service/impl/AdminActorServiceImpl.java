package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.dto.request.ChangeActorStatusRequest;
import dev.crepe.domain.admin.dto.response.ChangeActorStatusResponse;
import dev.crepe.domain.admin.dto.response.GetActorInfoResponse;
import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.admin.service.AdminActorService;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.model.ActorStatus;
import dev.crepe.domain.channel.actor.model.ActorSuspension;
import dev.crepe.domain.channel.actor.model.SuspensionType;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.service.impl.ActorServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminActorServiceImpl implements AdminActorService {

    private final ActorRepository actorRepository;
    private final ActorServiceImpl actorService;

    @Override
    @Transactional(readOnly = true)
    public Page<GetActorInfoResponse> getActorsByRole(String role, ActorStatus status, int page, int size) {
        UserRole enumRole;
        try {
            enumRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 역할입니다: " + role);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // status 파라미터에 따라 다른 쿼리 실행
        Page<Actor> actors;
        if (status != null) {
            actors = actorRepository.findByRoleAndActorStatus(enumRole, status, pageable);
        } else {
            actors = actorRepository.findByRole(enumRole, pageable);
        }

        return actors.map(actor -> {
            GetActorInfoResponse.GetActorInfoResponseBuilder builder = GetActorInfoResponse.builder()
                    .actorId(actor.getId())
                    .actorName(actor.getName())
                    .actorEmail(actor.getEmail())
                    .actorPhoneNum(actor.getPhoneNum())
                    .actorRole(actor.getRole().name())
                    .actorStatus(actor.getActorStatus().name());

            // 정지 정보가 있을 때만 suspensionInfo 매핑
            if (actor.getSuspension() != null) {
                ActorSuspension suspension = actor.getSuspension();

                // 정지 기간 계산
                String suspensionPeriod = calculateSuspensionPeriod(suspension);

                GetActorInfoResponse.SuspensionInfo suspensionInfo = GetActorInfoResponse.SuspensionInfo.builder()
                        .type(suspension.getType() != null ? suspension.getType().name() : null)
                        .suspendedAt(suspension.getSuspendedAt() != null ?
                                suspension.getSuspendedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                        .suspendedUntil(suspension.getSuspendedUntil() != null ?
                                suspension.getSuspendedUntil().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                        .reason(suspension.getReason())
                        .suspensionPeriod(suspensionPeriod)
                        .build();

                builder.suspensionInfo(suspensionInfo);
            }

            return builder.build();
        });
    }

    public ChangeActorStatusResponse changeActorStatus(ChangeActorStatusRequest request) {
        return actorService.changeActorStatus(request);
    }

    private String calculateSuspensionPeriod(ActorSuspension suspension) {
        if (suspension.getType() == SuspensionType.PERMANENT) {
            return "영구정지";
        }

        if (suspension.getSuspendedAt() != null && suspension.getSuspendedUntil() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = suspension.getSuspendedUntil();

            if (now.isAfter(endTime)) {
                return "정지 만료 (해제 필요)";
            }

            // 남은 일수 계산
            long remainingDays = ChronoUnit.DAYS.between(now.toLocalDate(), endTime.toLocalDate());
            long totalDays = ChronoUnit.DAYS.between(
                    suspension.getSuspendedAt().toLocalDate(),
                    endTime.toLocalDate()
            );

            if (remainingDays > 0) {
                return totalDays + "일 정지 (잔여: " + remainingDays + "일)";
            } else {
                return totalDays + "일 정지 (오늘 해제)";
            }
        }

        return "정지중";
    }
}
