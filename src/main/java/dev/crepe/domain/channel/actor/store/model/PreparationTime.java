package dev.crepe.domain.channel.actor.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import dev.crepe.global.error.exception.InvalidEnumValueException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PreparationTime {

    TEN(10, "10분"),
    TWENTY(20, "20분"),
    THIRTY(30, "30분"),
    FORTY(40, "40분"),
    FIFTY(50, "50분"),
    SIXTY(60, "60분");

    private final int minutes;
    private final String description;


    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static PreparationTime fromDescription(String description) {
        for (PreparationTime time : values()) {
            if (time.getDescription().equals(description)) {
                return time;
            }
        }
        throw new InvalidEnumValueException("PreparationTime", description);
    }

}
