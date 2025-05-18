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
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.global.model.dto.ApiResponse;
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
import java.util.Random;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class ActorServiceImpl  implements ActorService {

    private final ActorRepository actorRepository;
    private final ProductRepository productRepository;
    private final SubscribeRepository subscribeRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final SmsManageService smsManageService;
    private final Random random = new Random();
    private final ObjectMapper objectMapper;

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

    // 상품 구독
    @Override
    @Transactional
    public SubscribeProductResponse subscribeProduct(String userEmail, SubscribeProductRequest request) {
        Actor user = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // 이미 가입한 상품인지 확인
        if (subscribeRepository.existsByUserAndProduct(user, product)) {
            throw new IllegalStateException("이미 가입한 상품입니다.");
        }


        // 가입 자격 확인
        GetFinancialSummaryResponse financialSummary = GetFinancialSummaryResponse.builder()
                .userId(user.getId())
                .annualIncome(user.getAnnualIncome())
                .totalAsset(user.getTotalAsset())
                .build();
        checkEligibility(user, product, financialSummary);

        // 현재 시간과 만료일 계산 (1년 후)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredDate = now.plusYears(1);

        // 기본 구독 정보
        Subscribe.SubscribeBuilder subscribeBuilder = Subscribe.builder()
                .user(user)
                .product(product)
                .status(SubscribeStatus.ACTIVE)
                .subscribeDate(now)
                .expiredDate(expiredDate)
                .balance(BigDecimal.ZERO);

        // 상품 유형에 따른 추가 정보
        switch (product.getType()) {
            case SAVING:
                // 예금 상품 - 기본 이자율 적용
                subscribeBuilder.interestRate(calculateInterestRate(user, product));
                break;

            case INSTALLMENT:
                // 적금 상품 - 기본 이자율 적용
                subscribeBuilder.interestRate(calculateInterestRate(user, product));
                break;

            case VOUCHER:
                // 상품권 상품 - 이자율 없음, 상품권 코드 생성
                subscribeBuilder.interestRate(0);
                subscribeBuilder.voucherCode(generateVoucherCode());
                break;

            default:
                throw new IllegalStateException("지원하지 않는 상품 유형입니다.");
        }

        // 구독 저장
        Subscribe saved = subscribeRepository.save(subscribeBuilder.build());

        // 응답 생성
        SubscribeProductResponse.SubscribeProductResponseBuilder responseBuilder = SubscribeProductResponse.builder()
                .productName(product.getProductName())
                .productType(product.getType())
                .status(saved.getStatus())
                .subscribeDate(saved.getSubscribeDate())
                .expiredDate(saved.getExpiredDate())
                .balance(saved.getBalance())
                .interestRate(saved.getInterestRate())
                .message("상품 가입이 완료되었습니다.");

        // 상품권인 경우 코드 추가
        if (product.getType() == BankProductType.VOUCHER) {
            responseBuilder.voucherCode(saved.getVoucherCode());
        }

        return responseBuilder.build();
    }

    @Override
    public ResponseEntity<Void> addOccupationName(AddOccupationRequest request, String userEmail) {
        smsManageService.getSmsAuthData(request.getPhoneNumber(),SmsType.SUBSCRIBE_PRODUCT);
        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));
        actor.addOccupation(request.getOccupation());
        actorRepository.save(actor);
        return null;
    }

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
        actorRepository.save(actor);

        return new ResponseEntity<>("소득 정보가 성공적으로 조회되었습니다.", HttpStatus.OK);
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
    private boolean checkAgeEligibility(Actor user, EligibilityCriteria criteria) {
        // 모든 연령 허용이면 통과
        if (criteria.isAllAges()) {
            return true;
        }

        // 사용자 주민번호 앞자리로 연령대 결정
        String birthDate = user.getBirth();
        AgeGroup userAgeGroup = determineAgeGroup(birthDate);

        if (userAgeGroup == null) {
            return false; // 생년월일 정보가 유효하지 않으면 실패
        }

        // 해당 연령대가 조건에 포함되는지 확인
        return criteria.getAgeGroups().contains(userAgeGroup);
    }

    /**
     * 직업 조건 확인
     */
    private boolean checkOccupationEligibility(Actor user, EligibilityCriteria criteria) {
        // 모든 직업 허용이면 통과
        if (criteria.getOccupations().contains(Occupation.ALL_OCCUPATIONS)) {
            return true;
        }

        // 사용자 직업 확인
        Occupation userOccupation = user.getOccupation();

        return criteria.getOccupations().contains(userOccupation);
    }

    /**
     * 소득 수준 조건 확인
     */
    private boolean checkIncomeEligibility(GetFinancialSummaryResponse financialSummary, EligibilityCriteria criteria) {
        // 소득 제한 없음이면 통과
        if (criteria.hasNoIncomeLimit()) {
            return true;
        }

        // 연간 소득에 따른 소득 수준 확인
        BigDecimal annualIncome = financialSummary.getAnnualIncome();

        for (IncomeLevel incomeLevel : criteria.getIncomeLevels()) {
            if (incomeLevel == IncomeLevel.LOW_INCOME && annualIncome.compareTo(new BigDecimal("30000000")) <= 0) {
                // 연 소득 3000만원 이하는 저소득층
                return true;
            } else if (incomeLevel == IncomeLevel.LIMITED_INCOME &&
                    annualIncome.compareTo(new BigDecimal("60000000")) <= 0) {
                // 연 소득 6000만원 이하는 소득제한(월 5천 이하)
                return true;
            } else if (incomeLevel == IncomeLevel.NO_LIMIT) {
                // 제한 없음은 모든 소득 수준 허용
                return true;
            }
        }

        return false;
    }

    /**
     * 사용자가 상품 가입 조건에 부합하는지 확인
     */
    public void checkEligibility(Actor user, Product product, GetFinancialSummaryResponse financialSummary) {
        // 상품의 가입 조건 파싱
        EligibilityCriteria eligibilityCriteria;
        try {
            eligibilityCriteria = objectMapper.readValue(product.getJoinCondition(), EligibilityCriteria.class);
        } catch (IOException e) {
            throw new IllegalStateException("상품 가입 조건을 확인할 수 없습니다.");
        }

        // 연령 확인
        if (!checkAgeEligibility(user, eligibilityCriteria)) {
            throw new IllegalStateException("연령 조건에 부합하지 않습니다.");
        }

        // 직업 확인
        if (!checkOccupationEligibility(user, eligibilityCriteria)) {
            throw new IllegalStateException("직업 조건에 부합하지 않습니다.");
        }

        // 소득 수준 확인
        if (!checkIncomeEligibility(financialSummary, eligibilityCriteria)) {
            throw new IllegalStateException("소득 수준 조건에 부합하지 않습니다.");
        }
    }




    /**
     * 상품권 코드 생성
     */
    private String generateVoucherCode() {
        // UUID를 사용하여 고유 코드 생성
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    public static AgeGroup determineAgeGroup(String birthDate) {
        if (birthDate == null || birthDate.length() != 6) {
            return null;
        }

        try {
            // 년도, 월, 일 추출
            int year = Integer.parseInt(birthDate.substring(0, 2));
            int month = Integer.parseInt(birthDate.substring(2, 4));
            int day = Integer.parseInt(birthDate.substring(4, 6));

            // 생년월일 유효성 검사
            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }

            // 1900년대생인지 2000년대생인지 판별
            int fullYear = year < 100 ? (year >= 0 && year <= 24 ? 2000 + year : 1900 + year) : year;

            // 현재 날짜
            LocalDate now = LocalDate.now();
            LocalDate birth = LocalDate.of(fullYear, month, day);

            // 나이 계산
            int age = Period.between(birth, now).getYears();

            // AgeGroup 결정
            if (age >= 19 && age <= 34) {
                return AgeGroup.YOUTH;
            } else if (age >= 35 && age <= 64) {
                return AgeGroup.MIDDLE_AGED;
            } else if (age >= 65) {
                return AgeGroup.SENIOR;
            } else {
                // 19세 미만은 어떤 그룹에도 속하지 않음
                return null;
            }
        } catch (NumberFormatException | DateTimeException e) {
            // 숫자 변환 오류나 날짜 생성 오류
            return null;
        }
    }

    /**
     * 연령대에 따른 우대금리를 포함한 이자율 계산
     */
    private float calculateInterestRate(Actor user, Product product) {
        // 기본 이자율
        float baseRate = product.getBaseInterestRate();

        // 연령대에 따른 우대금리 계산
        AgeGroup userAgeGroup = determineAgeGroup(user.getBirth());
        if (userAgeGroup != null) {
            return switch (userAgeGroup) {
                case YOUTH -> baseRate + AgePreferentialRate.YOUTH.getRate().floatValue();
                case MIDDLE_AGED -> baseRate + AgePreferentialRate.MIDDLE_AGED.getRate().floatValue();
                case SENIOR -> baseRate + AgePreferentialRate.SENIOR.getRate().floatValue();
                default -> baseRate;
            };
        }

        return baseRate;
    }
}
