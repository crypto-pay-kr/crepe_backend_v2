package dev.crepe.infra.sms.model.dto.request;

import dev.crepe.infra.sms.model.SmsType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class VerifySmsCodeRequest {

    private String code;
    private String phone;
    private SmsType smsType;
}
