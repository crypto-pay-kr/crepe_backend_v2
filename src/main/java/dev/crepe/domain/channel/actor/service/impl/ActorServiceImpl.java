package dev.crepe.domain.channel.actor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.auth.jwt.AuthenticationToken;
import dev.crepe.domain.auth.jwt.JwtTokenProvider;
import dev.crepe.domain.auth.jwt.repository.TokenRepository;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import dev.crepe.domain.channel.actor.exception.*;
import dev.crepe.domain.channel.actor.model.dto.request.*;
import dev.crepe.domain.channel.actor.model.dto.response.GetFinancialSummaryResponse;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.EligibilityCriteria;
import dev.crepe.domain.core.product.model.dto.eligibility.IncomeLevel;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import dev.crepe.domain.core.product.model.dto.interest.AgePreferentialRate;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.subscribe.model.PreferentialRateModels;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.PreferentialConditionSatisfactionRepository;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.subscribe.util.PreferentialRateUtils;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class ActorServiceImpl  implements ActorService {

    private final ActorRepository actorRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final SmsManageService smsManageService;
    private final Random random = new Random();


    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {

        Actor actor = actorRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailedException::new);

        if (!encoder.matches(request.getPassword(), actor.getPassword())) {
            throw new LoginFailedException();
        }

        AuthenticationToken token;
        token = jwtTokenProvider.createToken(actor.getEmail(),actor.getRole());


        tokenRepository.findById(actor.getId())
                .ifPresentOrElse(
                        existingToken -> existingToken.updateTokens(token.getAccessToken(), token.getRefreshToken()),
                        () -> tokenRepository.save(new JwtToken(actor.getId(),actor.getRole(), token.getAccessToken(), token.getRefreshToken()))
                );

        TokenResponse tokenResponse = new TokenResponse(token, actor);
        return ApiResponse.success(actor.getRole() + " 로그인 성공", tokenResponse);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> changePassword(ChangePasswordRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        if(!encoder.matches(request.getOldPassword(), actor.getPassword())) {
            throw new InvalidPasswordException();
        }

        if(request.getOldPassword().equals(request.getNewPassword())) {
            throw new CannotSamePasswordException();
        }

        actor.changePassword(encoder.encode(request.getNewPassword()));
        return ResponseEntity.ok(null);
    }


    @Override
    @Transactional
    public ResponseEntity<Void> changePhone(ChangePhoneRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        InMemorySmsAuthService.SmsAuthData smsAuthData = smsManageService.getSmsAuthData(request.getPhoneNumber(), SmsType.SIGN_UP);
        String successNewPhone = smsAuthData.getPhoneNumber();

        actor.changePhone(successNewPhone);
        return ResponseEntity.ok(null);
    }


    @Override
    @Transactional
    public ResponseEntity<Void> changeName(ChangeNameRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        actor.changeName(request.getNewName());
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<Void> addOccupationName(AddOccupationRequest request, String userEmail) {
//        smsManageService.getSmsAuthData(request.getPhoneNumber(),SmsType.SUBSCRIBE_PRODUCT);
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));
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
    public ResponseEntity<String> checkIncome(String userEmail) {
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));
        if (actor.getAnnualIncome() != null && actor.getTotalAsset() != null) {
            return new ResponseEntity<>("이미 소득이 부여되었습니다", HttpStatus.BAD_REQUEST);
        }
        GetFinancialSummaryResponse financialData = getActorAsset(actor.getId());
        actor.addAnnualIncome(financialData.getAnnualIncome());
        actor.addTotalAsset(financialData.getTotalAsset());

        return new ResponseEntity<>("소득 정보가 성공적으로 조회되었습니다.", HttpStatus.OK);
    }

    @Transactional
    @Override
    public ResponseEntity<String> updateFromIdCard(String userEmail,IdCardOcrResponse idCardResponse) {
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if(actor.getName().equals(idCardResponse.getName())) {
            throw new IllegalArgumentException("등록된 이름과 신분증 이름이 일치하지 않습니다.");
        }
        actor.updateFromIdCard(idCardResponse);
        return new ResponseEntity<>("ocr 인증 성공", HttpStatus.OK);
    }


}
