package dev.crepe.infra.sms.model.dto.request;

import dev.crepe.infra.sms.model.SmsType;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SendSmsCodeRequest {

    @NotBlank
    private String phone;

    private SmsType smsType;
}
