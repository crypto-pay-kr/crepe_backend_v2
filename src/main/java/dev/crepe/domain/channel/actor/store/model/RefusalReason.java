package dev.crepe.domain.channel.actor.store.model;

import com.fasterxml.jackson.annotation.JsonValue;
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

}

