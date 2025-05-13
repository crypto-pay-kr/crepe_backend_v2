package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.AuthenticationToken;
import dev.crepe.domain.auth.jwt.JwtTokenProvider;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import dev.crepe.domain.auth.jwt.repository.TokenRepository;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.ChangeBankPhoneRequest;
import dev.crepe.domain.bank.model.dto.response.GetBankInfoDetailResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.bank.util.CheckAlreadyField;
import dev.crepe.domain.channel.actor.exception.LoginFailedException;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.store.exception.UnauthorizedStoreAccessException;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.global.util.NumberUtil;
import dev.crepe.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BankServiceImpl  implements BankService {

    private final BankRepository bankRepository;
    private final CheckAlreadyField checkAlreadyField;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final S3Service s3Service;


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

        AuthenticationToken token;
        token = jwtTokenProvider.createToken(bank.getEmail(),bank.getRole());


        tokenRepository.findById(bank.getId())
                .ifPresentOrElse(
                        existingToken -> existingToken.updateTokens(token.getAccessToken(), token.getRefreshToken()),
                        () -> tokenRepository.save(new JwtToken(bank.getId(),bank.getRole(), token.getAccessToken(), token.getRefreshToken()))
                );

        TokenResponse tokenResponse = new TokenResponse(token, bank);
        return ApiResponse.success(bank.getRole() + " 로그인 성공", tokenResponse);
    }



    // 은행 정보 조회
    @Override
    public GetBankInfoDetailResponse getBankAllDetails(String userEmail) {

        Bank bank = bankRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedStoreAccessException(userEmail));

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

        Bank bank = bankRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        String successNewPhone = NumberUtil.removeDash(request.getBankPhoneNumber());
        bank.changePhoneNum(successNewPhone);

        return ResponseEntity.ok(null);
    }



}
