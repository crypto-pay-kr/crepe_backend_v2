package dev.crepe.infra.sms.model.dto.response;


import lombok.*;


import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@RequiredArgsConstructor(access = PROTECTED)
@NoArgsConstructor(force = true)
public class  NhnSmsResponse {
    private final Header header;
    private final Body body;

    @Getter
    @RequiredArgsConstructor(access = PROTECTED)
    @NoArgsConstructor(force = true)
    public static class Header {
        private final boolean isSuccessful;
        private final int resultCode;
        private final String resultMessage;

        // isSuccessful은 @Getter tag로 인해 isSuccessful() getter가 만들어진다.
        // 그래서 파싱할 때 제대로 파싱이 안된다.
        // 따로 getIsSuccessful을 만들어주어야 함.
        public boolean getIsSuccessful() {
            return isSuccessful;
        }
    }

    @Getter
    @RequiredArgsConstructor(access = PROTECTED)
    @NoArgsConstructor(force = true)
    public static class Body {
        private final Data data;

        @Getter
        @RequiredArgsConstructor(access = PROTECTED)
        @NoArgsConstructor(force = true)
        public static class Data {
            private final String requestId;
            private final String statusCode;
            private final List<SendResult> sendResultList;

            @Getter
            @RequiredArgsConstructor(access = PROTECTED)
            @NoArgsConstructor(force = true)
            public static class SendResult {
                private final String recipientNo;
                private final int resultCode;
                private final String resultMessage;
            }
        }
    }
}