package dev.crepe.infra.otp.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import dev.crepe.infra.otp.model.dto.OtpSetupResponse;
import dev.crepe.infra.otp.model.entity.OtpCredential;
import dev.crepe.infra.otp.repository.OtpCredentialRepository;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final GoogleAuthenticator gAuth;
    private final OtpCredentialRepository otpCredentialRepository;
    private final ActorRepository actorRepository;
    private final ExceptionDbService exceptionDbService;

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

    public String generateQRUrl(String email, GoogleAuthenticatorKey key) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("Crepe", email, key);
    }

    public boolean verifyCode(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }

    @Transactional
    public void enableOtp(Long userId) {
        OtpCredential credential = otpCredentialRepository.findByUserId(userId)
                .orElseThrow(() -> exceptionDbService.getException("OTP_002")); // OTP 설정이 없습니다.

        credential.setEnabled(true);
        otpCredentialRepository.save(credential);
    }

    public OtpCredential getOtpCredential(String email) {
        Actor actor = actorRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("OTP_001")); // 사용자를 찾을 수 없습니다.

        return otpCredentialRepository.findByUserId(actor.getId())
                .orElse(null);
    }

    @Transactional
    public ApiResponse<OtpSetupResponse> setupOtp(String email) {
        Actor actor = actorRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("OTP_001")); // 사용자를 찾을 수 없습니다.

        GoogleAuthenticatorKey key = generateAndSaveKey(actor.getId());
        String qrCodeUrl = generateQRUrl(email, key);

        OtpSetupResponse response = new OtpSetupResponse(key.getKey(), qrCodeUrl);
        return ApiResponse.success("OTP 설정 정보", response);
    }

    @Transactional
    public ApiResponse<Boolean> verifyAndEnableOtp(String email, int otpCode) {
        Actor actor = actorRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("OTP_001")); // 사용자를 찾을 수 없습니다.

        OtpCredential credential = getOtpCredential(email);
        if (credential == null) {
            throw exceptionDbService.getException("OTP_005"); // OTP가 설정되지 않았습니다.
        }

        boolean isValid = verifyCode(credential.getSecretKey(), otpCode);

        if (isValid) {
            enableOtp(actor.getId());
            return ApiResponse.success("OTP가 성공적으로 활성화되었습니다", true);
        } else {
            throw exceptionDbService.getException("OTP_003"); // 잘못된 OTP 코드입니다.
        }
    }

    @Transactional
    public ApiResponse<OtpCredential> getOtpStatus(String email) {
        OtpCredential credential = getOtpCredential(email);

        if (credential == null) {
            return ApiResponse.success("OTP 미설정", null);
        }

        return ApiResponse.success("OTP 상태 조회 성공", credential);
    }

    @Transactional
    public ApiResponse<Boolean> deleteOtp(String email) {
        try {
            Actor actor = actorRepository.findByEmail(email)
                    .orElseThrow(() -> exceptionDbService.getException("OTP_001")); // 사용자를 찾을 수 없습니다.

            OtpCredential credential = otpCredentialRepository.findByUserId(actor.getId()).orElse(null);

            if (credential == null) {
                throw exceptionDbService.getException("OTP_005"); // OTP가 설정되지 않았습니다.
            }

            if (!credential.isEnabled()) {
                throw exceptionDbService.getException("OTP_004"); // OTP가 이미 비활성화되어 있습니다.
            }

            otpCredentialRepository.delete(credential);
            return ApiResponse.success("OTP 해제 성공", true);
        } catch (Exception e) {
            throw exceptionDbService.getException("OTP_006"); // OTP 해제 중 오류 발생
        }
    }
}