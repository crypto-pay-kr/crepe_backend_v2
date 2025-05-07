package dev.crepe.domain.channel.actor.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import dev.crepe.global.error.exception.InvalidEnumValueException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefusalReason {
    OUT_OF_STOCK("재고없음"),
    UNABLE_TO_SEAT("자리없음"),
    UNABLE_TO_PREPARE("주문많음");

    private final String description;


    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static RefusalReason fromDescription(String description) {
        for (RefusalReason reason : values()) {
            if (reason.getDescription().equals(description)) {
                return reason;
            }
        }
        throw new InvalidEnumValueException("RefusalReason", description);
    }
}

