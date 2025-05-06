package dev.crepe.infra.sms.model.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class NhnSmsRequest {

    private final String body;
    private final String sendNo;
    private final List<Recipient> recipientList;

    public NhnSmsRequest(String senderPhone, String phone, String body) {
        this.body = body;
        this.sendNo = senderPhone;
        this.recipientList = List.of(new Recipient(phone));
    }

    @Getter
    public static class Recipient {
        private final String internationalRecipientNo;

        public Recipient(String recipientNo) {
            // 전화번호에 하이픈(-)을 넣어도 자동으로 제거
            this.internationalRecipientNo = recipientNo.replaceAll("-", "");
        }
    }
}

