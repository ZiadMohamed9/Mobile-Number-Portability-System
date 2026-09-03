package com.fourgtss.mnp.exception;

import com.fourgtss.mnp.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<PortingRequestErrorCode, HttpStatus> STATUS_MAP = Map.ofEntries(
            Map.entry(PortingRequestErrorCode.UNKNOWN_ORGANIZATION, HttpStatus.BAD_REQUEST),
            Map.entry(PortingRequestErrorCode.MOBILE_NUMBER_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(PortingRequestErrorCode.PORTING_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(PortingRequestErrorCode.NATIONAL_ID_MISMATCH, HttpStatus.FORBIDDEN),
            Map.entry(PortingRequestErrorCode.NUMBER_NOT_ACTIVE, HttpStatus.CONFLICT),
            Map.entry(PortingRequestErrorCode.SAME_OPERATOR, HttpStatus.CONFLICT),
            Map.entry(PortingRequestErrorCode.MINIMUM_TENURE_NOT_MET, HttpStatus.CONFLICT),
            Map.entry(PortingRequestErrorCode.PENDING_REQUEST_EXISTS, HttpStatus.CONFLICT),
            Map.entry(PortingRequestErrorCode.NOT_DONOR, HttpStatus.FORBIDDEN),
            Map.entry(PortingRequestErrorCode.DONOR_NO_LONGER_CURRENT, HttpStatus.CONFLICT),
            Map.entry(PortingRequestErrorCode.REQUEST_NOT_PENDING, HttpStatus.CONFLICT)
    );

    private static final Map<MobileNumberErrorCode, HttpStatus> MOBILE_NUMBER_STATUS_MAP = Map.ofEntries(
            Map.entry(MobileNumberErrorCode.PHONE_NUMBER_ALREADY_EXISTS, HttpStatus.CONFLICT),
            Map.entry(MobileNumberErrorCode.OPERATOR_PREFIX_MISMATCH, HttpStatus.BAD_REQUEST),
            Map.entry(MobileNumberErrorCode.FUTURE_DATE, HttpStatus.BAD_REQUEST),
            Map.entry(MobileNumberErrorCode.UNKNOWN_OPERATOR, HttpStatus.BAD_REQUEST)
    );

    @ExceptionHandler(PortingRequestException.class)
    public ResponseEntity<ApiErrorResponse> handlePortingRequest(PortingRequestException exception) {
        PortingRequestErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = STATUS_MAP.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(errorCode.name(), toHumanReadable(errorCode.name())));
    }

    @ExceptionHandler(MobileNumberException.class)
    public ResponseEntity<ApiErrorResponse> handleMobileNumber(MobileNumberException exception) {
        MobileNumberErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = MOBILE_NUMBER_STATUS_MAP.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(errorCode.name(), toHumanReadable(errorCode.name())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DATA_CONFLICT", "The operation conflicts with existing data"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("MALFORMED_REQUEST", "Request body is missing or malformed"));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("MISSING_HEADER", "Required header '" + exception.getHeaderName() + "' is missing"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        "INVALID_PARAMETER",
                        "Invalid value for parameter '" + exception.getName() + "'"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unexpected error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private static String toHumanReadable(String enumName) {
        return enumName.charAt(0)
                + enumName.substring(1).toLowerCase().replace('_', ' ');
    }
}
