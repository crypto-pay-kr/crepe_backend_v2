package dev.crepe.global.error.exception;

import org.springframework.stereotype.Service;

import dev.crepe.global.error.exception.model.ExceptionStatus;
import lombok.RequiredArgsConstructor;
import dev.crepe.global.error.exception.model.ExceptionDb;

@Service
@RequiredArgsConstructor
public class ExceptionDbService {
    
    private final ExceptionDbRepository exceptionDbRepository;

    public CustomException getException(String code) {
        ExceptionDb exception = exceptionDbRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exception not found for code: " + code));

        return new CustomException(code, exception.getStatus(), exception.getMessage());
    }

    public void throwException(String code) {
        throw getException(code);
    }

    public CustomException throwExceptionAndReturn(String code) {
        return getException(code);
    }



}
