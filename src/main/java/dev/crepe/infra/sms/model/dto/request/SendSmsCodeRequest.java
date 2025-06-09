package dev.crepe.infra.sms.model.dto.request;

import dev.crepe.infra.sms.model.SmsType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class SendSmsCodeRequest {

    @NotBlank
    private String phone;

    private SmsType smsType;
}
