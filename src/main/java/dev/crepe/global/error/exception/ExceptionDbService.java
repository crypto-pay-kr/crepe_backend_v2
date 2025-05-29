package dev.crepe.global.error.exception;

import org.springframework.stereotype.Service;

import dev.crepe.global.error.exception.model.ExceptionStatus;
import lombok.RequiredArgsConstructor;
import dev.crepe.global.error.exception.model.ExceptionDb;

@Service
@RequiredArgsConstructor
public class ExceptionDbService {
    
    private final ExceptionDbRepository exceptionDbRepository;

    public String getExceptionMessage(int code, ExceptionStatus status) {
        return exceptionDbRepository.findByCodeAndStatus(code, status)
                .map(ExceptionDb::getMessage)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exception message not found for code: " + code + " and status: " + status
                ));
    }

}
