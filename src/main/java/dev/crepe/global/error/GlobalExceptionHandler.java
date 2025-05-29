package dev.crepe.global.error;

import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @ExceptionHandler(LocalizedMessageException.class)
    public ResponseEntity<ErrorResponse> handleLocalizedMessageException(
            LocalizedMessageException ex,
            WebRequest request,
            Locale locale) {
        String localizedMessage = ex.getDefaultMessage(messageSource, locale);
        ErrorResponse response = new ErrorResponse(
                ex.getStatusCode(),
                ex.getCode(),
                localizedMessage,
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<HashMap<String, Object>> handleLocalException(CustomException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatus().getCode());
        body.put("code", ex.getCode());
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        HttpStatus httpStatus = HttpStatus.resolve(ex.getStatus().getCode());

        return new ResponseEntity<>(new HashMap<>(body), httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR);
    }


//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
//
//        ErrorResponse response = new ErrorResponse(
//                500,
//                "UnexpectedException",
//                "예기치 못한 오류가 발생했습니다.",
//                LocalDateTime.now()
//        );
//        return ResponseEntity.status(500).body(response);
//    }


}