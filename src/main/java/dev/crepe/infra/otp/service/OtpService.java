package dev.crepe.infra.otp.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import dev.crepe.infra.otp.model.dto.OtpSetupResponse;
import dev.crepe.infra.otp.model.entity.OtpCredential;
import dev.crepe.infra.otp.repository.OtpCredentialRepository;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.global.model.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final GoogleAuthenticator gAuth;
    private final OtpCredentialRepository otpCredentialRepository;
    private final ActorRepository actorRepository;



    @Transactional
    public GoogleAuthenticatorKey generateAndSaveKey(Long userId) {

        GoogleAuthenticatorKey key = gAuth.createCredentials();

        otpCredentialRepository.findByUserId(userId)
                .ifPresentOrElse(
                        credential -> credential.updateSecretKey(key.getKey()),
                        () -> otpCredentialRepository.save(new OtpCredential(userId, key.getKey()))
                );

        return key;
    }

    // QR 코드 URL 생성
    public String generateQRUrl(String email, GoogleAuthenticatorKey key) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("Crepe", email, key);
    }

    // OTP 코드 검증
    public boolean verifyCode(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }

    // OTP 활성화
    @Transactional
    public void enableOtp(Long userId) {
        OtpCredential credential = otpCredentialRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("OTP 설정이 없습니다"));

        credential.setEnabled(true);
        otpCredentialRepository.save(credential);
    }

    // OTP 상태 및 비밀키 조회
    public OtpCredential getOtpCredential(Long userId) {
        return otpCredentialRepository.findByUserId(userId)
                .orElse(null);
    }

    // OTP 설정 메서드
    @Transactional
    public ApiResponse<OtpSetupResponse> setupOtp(String email) {
        Actor actor = actorRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        GoogleAuthenticatorKey key = generateAndSaveKey(actor.getId());

        String qrCodeUrl = generateQRUrl(email, key);

        OtpSetupResponse response = new OtpSetupResponse(
                key.getKey(),
                qrCodeUrl
        );

        return ApiResponse.success("OTP 설정 정보", response);
    }

    // OTP 활성화 메서드
    @Transactional
    public ApiResponse<Boolean> verifyAndEnableOtp(String email, int otpCode) {
        Actor actor = actorRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        OtpCredential credential = getOtpCredential(actor.getId());
        if (credential == null) {
            throw new RuntimeException("OTP가 설정되지 않았습니다");
        }

        // OTP 코드 검증
        boolean isValid = verifyCode(credential.getSecretKey(), otpCode);

        if (isValid) {
            // OTP 활성화
            enableOtp(actor.getId());
            return ApiResponse.success("OTP가 성공적으로 활성화되었습니다", true);
        } else {
            return ApiResponse.fail("잘못된 OTP 코드입니다");
        }
    }
}
