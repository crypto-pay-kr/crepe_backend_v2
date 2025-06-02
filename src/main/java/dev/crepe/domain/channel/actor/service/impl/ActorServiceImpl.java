package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.admin.dto.request.ChangeActorStatusRequest;
import dev.crepe.domain.admin.dto.response.ChangeActorStatusResponse;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;
import dev.crepe.domain.auth.jwt.repository.TokenRepository;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import dev.crepe.domain.auth.sse.service.impl.AuthServiceImpl;
import dev.crepe.domain.channel.actor.exception.*;
import dev.crepe.domain.channel.actor.model.ActorStatus;
import dev.crepe.domain.channel.actor.model.ActorSuspension;
import dev.crepe.domain.channel.actor.model.RoleCountProjection;
import dev.crepe.domain.channel.actor.model.SuspensionType;
import dev.crepe.domain.channel.actor.model.dto.request.*;
import dev.crepe.domain.channel.actor.model.dto.response.GetFinancialSummaryResponse;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.infra.naver.ocr.id.entity.dto.IdCardOcrResponse;
import dev.crepe.infra.sms.model.InMemorySmsAuthService;
import dev.crepe.infra.sms.model.SmsType;
import dev.crepe.infra.sms.service.SmsManageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ActorServiceImpl  implements ActorService {

    private final ActorRepository actorRepository;
    private final PasswordEncoder encoder;
    private final AuthServiceImpl authService;
    private final SmsManageService smsManageService;
    private final Random random = new Random();
    private final AccountService accountService;
    private final ExceptionDbService exceptionDbService;

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        if (actorRepository.existsByEmail(email)) {
            throw exceptionDbService.getException("ACTOR_003");
        }
        return false;
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        if (actorRepository.existsByNickName(nickname)) {
            throw exceptionDbService.getException("ACTOR_004");
        }
        return false;
    }

    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // 사용자 존재 여부 먼저 확인
        Optional<Actor> actorOptional = actorRepository.findByEmail(request.getEmail());
        if (actorOptional.isEmpty()) {
            log.warn("No user found with email: {}", request.getEmail());
            throw exceptionDbService.getException("ACTOR_002");
        }

        Actor actor = actorOptional.get();
        log.info("User found: {}, checking password...", actor.getEmail());

        if (!encoder.matches(request.getPassword(), actor.getPassword())) {
            log.warn("Password does not match for email: {}", request.getEmail());
            throw exceptionDbService.getException("ACTOR_005");
        }

        AuthenticationToken token = authService.createAndSaveToken(actor.getEmail(), actor.getRole());

        TokenResponse tokenResponse = new TokenResponse(token, actor);

        // 로그인 시 기본 코인 계좌 생성
        accountService.createBasicAccounts(request.getEmail());

        return ApiResponse.success(actor.getRole() + " 로그인 성공", tokenResponse);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> changePassword(ChangePasswordRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        if(!encoder.matches(request.getOldPassword(), actor.getPassword())) {
            throw exceptionDbService.getException("ACTOR_006");
        }

        if(request.getOldPassword().equals(request.getNewPassword())) {
            throw exceptionDbService.getException("ACTOR_011");
        }

        actor.changePassword(encoder.encode(request.getNewPassword()));
        return ResponseEntity.ok(null);
    }


    @Override
    @Transactional
    public ResponseEntity<Void> changePhone(ChangePhoneRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        InMemorySmsAuthService.SmsAuthData smsAuthData = smsManageService.getSmsAuthData(request.getPhoneNumber(), SmsType.SIGN_UP);
        String successNewPhone = smsAuthData.getPhoneNumber();

        actor.changePhone(successNewPhone);
        return ResponseEntity.ok(null);
    }


    @Override
    @Transactional
    public ResponseEntity<Void> changeName(ChangeNameRequest request, String userEmail) {
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        actor.changeName(request.getNewName());
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<Void> addOccupationName(AddOccupationRequest request, String userEmail) {
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));
        actor.addOccupation(request.getOccupation());
        actorRepository.save(actor);
        return ResponseEntity.ok(null);
    }

    // 소득 랜덤 부여 함수
    public GetFinancialSummaryResponse getActorAsset(Long userId) {
        int incomeGroup = random.nextInt(3);

        BigDecimal annualIncome;
        BigDecimal totalAsset = switch (incomeGroup) {
            case 0 -> {
                // 연 소득 1200만원 ~ 3000만원
                annualIncome = new BigDecimal(random.nextInt(18000000) + 12000000);
                // 자산 500만원 ~ 3000만원
                yield new BigDecimal(random.nextInt(25000000) + 5000000);
            }
            case 1 -> {
                // 연 소득 3000만원 초과 ~ 6000만원 이하 (월 250만 ~ 500만)
                annualIncome = new BigDecimal(random.nextInt(30000000) + 30000001);
                // 자산 3000만원 ~ 1억
                yield new BigDecimal(random.nextInt(70000000) + 30000000);
            }
            default -> {
                // 연 소득 6000만원 초과
                annualIncome = new BigDecimal(random.nextInt(140000000) + 60000001);
                // 자산 1억 ~ 10억
                yield new BigDecimal(random.nextInt(900000000) + 100000000);
            }
        };

        return GetFinancialSummaryResponse.builder()
                .userId(userId)
                .annualIncome(annualIncome)
                .totalAsset(totalAsset)
                .build();
    }

    @Transactional
    @Override
    public GetFinancialSummaryResponse checkIncome(String userEmail) {
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        if (actor.getAnnualIncome() != null && actor.getTotalAsset() != null) {
            return GetFinancialSummaryResponse.builder()
                    .userId(actor.getId())
                    .annualIncome(actor.getAnnualIncome())
                    .totalAsset(actor.getTotalAsset())
                    .build();
        }
        GetFinancialSummaryResponse financialData = getActorAsset(actor.getId());
        actor.addAnnualIncome(financialData.getAnnualIncome());
        actor.addTotalAsset(financialData.getTotalAsset());

        return financialData;
    }

    @Transactional
    @Override
    public ResponseEntity<String> updateFromIdCard(String userEmail, IdCardOcrResponse idCardResponse) {
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(()  -> exceptionDbService.getException("ACTOR_002")
                );
        if (!actor.getName().equals(idCardResponse.getName())) {
            throw exceptionDbService.getException("ACTOR_007");
        }

        try {
            actor.updateFromIdCard(idCardResponse);
            actorRepository.save(actor);
            log.info("사용자 정보 업데이트 완료");
            return new ResponseEntity<>("OCR 인증 성공", HttpStatus.OK);
        } catch (Exception e) {
            log.error("업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 역할별 counts
    public Map<String, Long> getRoleCounts() {
        List<RoleCountProjection> counts = actorRepository.countActorsByRole();
        return counts.stream()
                .collect(Collectors.toMap(RoleCountProjection::getRole, RoleCountProjection::getCount));
    }

    @Override
    public ChangeActorStatusResponse changeActorStatus(ChangeActorStatusRequest request) {
        Actor actor = actorRepository.findById(request.getActorId())
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        String message;
        ChangeActorStatusResponse.SuspensionInfo suspensionInfo = null;

        if ("SUSPEND".equals(request.getAction())) {
            // 정지 처리
            actor.changeActorStatus(ActorStatus.SUSPENDED);

            if (request.getSuspensionRequest() != null) {
                ChangeActorStatusRequest.SuspensionRequest suspReq = request.getSuspensionRequest();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endDate = null;

                if (suspReq.getType() == SuspensionType.TEMPORARY && suspReq.getDays() != null) {
                    endDate = now.plusDays(suspReq.getDays());
                }

                ActorSuspension suspension = ActorSuspension.builder()
                        .type(suspReq.getType())
                        .suspendedAt(now)
                        .suspendedUntil(endDate)
                        .reason(suspReq.getReason())
                        .build();

                actor.changeSuspension(suspension);

                suspensionInfo = ChangeActorStatusResponse.SuspensionInfo.builder()
                        .type(suspReq.getType())
                        .startDate(now)
                        .endDate(endDate)
                        .reason(suspReq.getReason())
                        .build();

                message = suspReq.getType() == SuspensionType.PERMANENT
                        ? "계정이 영구정지되었습니다."
                        : suspReq.getDays() + "일 정지되었습니다.";
            } else {
                message = "계정이 정지되었습니다.";
            }

        } else if ("UNSUSPEND".equals(request.getAction())) {
            // 정지 해제 처리
            actor.changeActorStatus(ActorStatus.ACTIVE);
            actor.changeSuspension(null); // 정지 정보 제거
            message = "계정 정지가 해제되었습니다.";

        } else {
            throw exceptionDbService.getException("ACTOR_012");
        }

        actorRepository.save(actor); // 저장 필수!

        return ChangeActorStatusResponse.builder()
                .userId(actor.getId())
                .message(message)
                .actorStatus(actor.getActorStatus())
                .suspensionInfo(suspensionInfo)
                .build();
    }

}
