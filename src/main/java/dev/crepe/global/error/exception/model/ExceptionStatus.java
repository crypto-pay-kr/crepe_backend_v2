package dev.crepe.global.error.exception.model;

public enum ExceptionStatus {
    // 2xx Success
    SUCCESS(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),
    
    // 3xx Redirection
    MULTIPLE_CHOICES(300),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    SEE_OTHER(303),
    NOT_MODIFIED(304),
    TEMPORARY_REDIRECT(307),
    PERMANENT_REDIRECT(308),
    
    // 4xx Client Error
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    
    // 5xx Server Error
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504);
    
    private final int code;
    
    ExceptionStatus(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}