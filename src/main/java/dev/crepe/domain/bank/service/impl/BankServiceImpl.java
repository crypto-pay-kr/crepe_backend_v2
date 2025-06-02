package dev.crepe.domain.bank.service.impl;


import dev.crepe.domain.admin.dto.request.ChangeBankStatusRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankResponse;
import dev.crepe.domain.admin.dto.response.GetAllSuspendedBankResponse;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.sse.service.impl.AuthServiceImpl;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.ChangeBankPhoneRequest;
import dev.crepe.domain.bank.model.dto.response.GetBankInfoDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.model.entity.BankStatus;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.bank.util.CheckAlreadyField;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.global.util.NumberUtil;
import dev.crepe.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final BankRepository bankRepository;
    private final S3Service s3Service;
    private final AccountService accountService;
    private final CheckAlreadyField checkAlreadyField;
    private final AuthServiceImpl authService;
    private final PasswordEncoder encoder;
    private final ExceptionDbService exceptionDbService;
    private final BankTokenRepository bankTokenRepository;

    // 은행 회원가입
    @Override
    @Transactional
    public ApiResponse<ResponseEntity<Void>> signup(BankDataRequest request) {
        log.info("회원가입 요청: {}", request);
        checkAlreadyField.validate(request);


        String bankImageUrl = s3Service.uploadFile(request.getBankCiImage(), "bank-images");

        // 대시 제거 유틸 함수 사용
        String plainPhoneNum = NumberUtil.removeDash(
                request.getBankSignupDataRequest().getBankPhoneNum()
        );


        Bank bank = Bank.builder()
                .email(request.getBankSignupDataRequest().getEmail())
                .password(encoder.encode(request.getBankSignupDataRequest().getPassword()))
                .bankPhoneNum(plainPhoneNum)
                .name(request.getBankSignupDataRequest().getName())
                .imageUrl(bankImageUrl)
                .bankCode(request.getBankSignupDataRequest().getBankCode())
                .managerName(request.getBankSignupDataRequest().getManagerName())
                .role(UserRole.BANK)
                .build();

        bankRepository.save(bank);

        return ApiResponse.success("회원가입 성공", null);
    }


    // 은행 로그인
    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {
        log.info("로그인 요청: {}", request);
        Bank bank = bankRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));

        if (!encoder.matches(request.getPassword(), bank.getPassword())) {
            throw exceptionDbService.getException("BANK_008");
        }

        // AuthService를 통해 토큰 생성 및 저장 (중복 로그인 방지 + 실시간 알림)
        AuthenticationToken token = authService.createAndSaveToken(bank.getEmail(), bank.getRole());

        // 기본 계좌 생성
        accountService.createBasicBankAccounts(bank);
        TokenResponse tokenResponse = new TokenResponse(token, bank);
        return ApiResponse.success(bank.getRole() + " 로그인 성공", tokenResponse);
    }


    // 은행 정보 조회
    @Override
    public GetBankInfoDetailResponse getBankAllDetails(String bankEmail) {

        Bank bank = findBankInfoByEmail(bankEmail);


        GetBankInfoDetailResponse  res =
                GetBankInfoDetailResponse .builder()
                        .bankId(bank.getId())
                        .bankEmail(bank.getEmail())
                        .bankName(bank.getName())
                        .bankPhoneNumber(bank.getBankPhoneNum())
                        .bankImageUrl(bank.getImageUrl())
                        .bankCode(bank.getBankCode())
                        .build();

        return res;
    }


    // 은행 담당자 번호 수정
    @Override
    @Transactional
    public ResponseEntity<Void> changePhone(ChangeBankPhoneRequest request, String userEmail) {
        log.info("전화번호 변경 요청: {}, 사용자 이메일: {}", request, userEmail);
        Bank bank = findBankInfoByEmail(userEmail);

        String successNewPhone = NumberUtil.removeDash(request.getBankPhoneNumber());
        bank.changePhoneNum(successNewPhone);

        return ResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public void changeBankCI(MultipartFile ciImage, String bankEmail) {
        log.info("CI 이미지 변경 요청: {}, 은행 이메일: {}", ciImage.getOriginalFilename(), bankEmail);
        Bank bank = findBankInfoByEmail(bankEmail);

        // S3에 이미지 업로드
        String newImageUrl = s3Service.uploadFile(ciImage, "bank-images");

        // 이미지 URL 업데이트
        bank.changeCiImageUrl(newImageUrl);
        bankRepository.save(bank);
    }


    // 관리자 전체 은행 조회
    @Override
    public List<GetAllBankResponse> getAllActiveBankList() {
        // ACTIVE 상태
        List<Bank> banks = bankRepository.findByStatus(BankStatus.ACTIVE);

        return banks.stream()
                .map(bank -> {
                    List<BankToken> bankTokens = bankTokenRepository.findByBankId(bank.getId());

                    BigDecimal totalSupply = bankTokens.stream()
                            .map(BankToken::getTotalSupply)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return GetAllBankResponse.builder()
                            .id(bank.getId())
                            .name(bank.getName())
                            .bankPhoneNum(bank.getBankPhoneNum())
                            .imageUrl(bank.getImageUrl())
                            .totalSupply(totalSupply)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void changeBankStatus(ChangeBankStatusRequest request) {
        log.info("은행 상태 변경 요청: {}", request);
        Bank bank = bankRepository.findById(request.getBankId())
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));

        BankStatus currentStatus = bank.getStatus();
        BankStatus newStatus = request.getBankStatus();

        if (currentStatus == newStatus) {
            throw exceptionDbService.getException("BANK_002");
        }

        bank.changeStatus(newStatus);
        bankRepository.save(bank);
    }

    @Override
    public List<GetAllSuspendedBankResponse> getAllSuspendedBankList() {
        List<Bank> banks = bankRepository.findByStatus(BankStatus.SUSPENDED);
        return banks.stream()
                .map(bank -> {
                    List<BankToken> bankTokens = bankTokenRepository.findByBankId(bank.getId());

                    BigDecimal totalSupply = bankTokens.stream()
                            .map(BankToken::getTotalSupply)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return GetAllSuspendedBankResponse.builder()
                            .id(bank.getId())
                            .name(bank.getName())
                            .suspendedDate(bank.getSuspendedDate())
                            .bankPhoneNum(bank.getBankPhoneNum())
                            .totalSupply(totalSupply)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GetCoinAccountInfoResponse> getBankAccountByAdmin(Long bankId) {
        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));


        List<Account> accounts = accountService.getAccountsByBankEmail(bank.getEmail());

        if (accounts.isEmpty()) {
            throw exceptionDbService.getException("ACCOUNT_001");
        }

        return accounts.stream()
                .filter(a -> a.getCoin() != null) // 코인 정보가 없는 계좌는 제외
                .map(a -> GetCoinAccountInfoResponse.builder()
                        .bankName(a.getBank() != null ? a.getBank().getName() : null)
                        .coinName(a.getCoin().getName())
                        .currency(a.getCoin().getCurrency())
                        .accountAddress(a.getAccountAddress())
                        .tag(a.getTag())
                        .balance(a.getBalance().toPlainString())
                        .status(a.getAddressRegistryStatus())
                        .build())
                .collect(Collectors.toList());
    }


    @Override
    public Bank findBankInfoByEmail(String email) {
        return bankRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));
    }


}

