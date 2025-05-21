package dev.crepe.domain.bank.service.impl;


import dev.crepe.domain.admin.dto.request.ChangeBankStatusRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankResponse;
import dev.crepe.domain.admin.dto.response.GetAllSuspendedBankResponse;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.sse.service.impl.AuthServiceImpl;
import dev.crepe.domain.bank.exception.BankNotFoundException;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.ChangeBankPhoneRequest;
import dev.crepe.domain.bank.model.dto.response.GetBankInfoDetailResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.model.entity.BankStatus;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.bank.util.CheckAlreadyField;
import dev.crepe.domain.channel.actor.exception.LoginFailedException;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.global.util.NumberUtil;
import dev.crepe.infra.s3.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

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
    private final BankTokenRepository bankTokenRepository;

    // 은행 회원가입
    @Override
    @Transactional
    public ApiResponse<ResponseEntity<Void>> signup(BankDataRequest request) {

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
                .role(UserRole.BANK)
                .build();

        bankRepository.save(bank);

        // 기본 계좌 생성
        accountService.createBasicBankAccounts(bank);

        return ApiResponse.success("회원가입 성공", null);
    }


    // 은행 로그인
    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {
        Bank bank = bankRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailedException::new);

        if (!encoder.matches(request.getPassword(), bank.getPassword())) {
            throw new LoginFailedException();
        }

        // AuthService를 통해 토큰 생성 및 저장 (중복 로그인 방지 + 실시간 알림)
        AuthenticationToken token = authService.createAndSaveToken(bank.getEmail(), bank.getRole());

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

        Bank bank = findBankInfoByEmail(userEmail);

        String successNewPhone = NumberUtil.removeDash(request.getBankPhoneNumber());
        bank.changePhoneNum(successNewPhone);

        return ResponseEntity.ok(null);
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
                            .totalSupply(totalSupply)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void changeBankStatus(ChangeBankStatusRequest request) {
        Bank bank = bankRepository.findById(request.getBankId())
                .orElseThrow(() -> new EntityNotFoundException("해당 은행을 찾을 수 없습니다. ID: " + request.getBankId()));

        BankStatus currentStatus = bank.getStatus();
        BankStatus newStatus = request.getBankStatus();

        if (currentStatus == newStatus) {
            throw new IllegalStateException("은행이 이미 " + newStatus + " 상태입니다.");
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
    public Bank findBankInfoByEmail(String email) {
        return bankRepository.findByEmail(email)
                .orElseThrow(() -> new BankNotFoundException(email));
    }


 

}

