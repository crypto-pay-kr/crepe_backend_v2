package dev.crepe.domain.channel.actor.store;


import dev.crepe.domain.channel.actor.store.model.PreparationTime;
import dev.crepe.domain.channel.actor.store.model.RefusalReason;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnumValidationService {
    private final ExceptionDbService exceptionDbService;

    public PreparationTime validatePreparationTime(PreparationTime time) {
        if (time == null) {
            throw exceptionDbService.getException("STORE_ORDER_001");
        }
        return time;
    }

    public RefusalReason validateRefusalReason(RefusalReason reason) {
        if (reason == null) {
            throw exceptionDbService.getException("STORE_ORDER_002");
        }
        return reason;
    }
}