package dev.crepe.infra.otp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OtpSetupResponse {
    private String secretKey;
    private String qrCodeUrl;
}
