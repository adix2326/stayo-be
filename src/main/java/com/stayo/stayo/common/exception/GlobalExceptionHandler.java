package com.stayo.stayo.common.exception;

import com.stayo.stayo.common.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidMobileNumberException.class)
    public ResponseEntity<ApiError> handleInvalidMobileNumber(InvalidMobileNumberException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Invalid Mobile Number", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<ApiError> handleOtpNotFound(OtpNotFoundException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.NOT_FOUND.value(), "OTP Not Found", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.NOT_FOUND.value(), "OTP Not Found", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiError> handleOtpExpired(OtpExpiredException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.GONE.value(), "OTP Expired", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.GONE).body(body);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Invalid OTP", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MaxOtpAttemptsExceededException.class)
    public ResponseEntity<ApiError> handleMaxAttempts(MaxOtpAttemptsExceededException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    // Fallback for unexpected exceptions (optional)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // Handle @Valid method argument validation failures (request body)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation Failed", details, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handle constraint violations (e.g. @Validated on path/query params)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String details = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation Failed", details, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handle malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String errorMessage = ex.getMostSpecificCause().getMessage();
        if(errorMessage != null && errorMessage.contains(":"))
            errorMessage = errorMessage.substring(0, errorMessage.indexOf(":")).trim();
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Malformed JSON", errorMessage, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingAuthorizationException.class)
    public ResponseEntity<ApiError> handleMissingAuthorization(MissingAuthorizationException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Missing Authorization", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidToken(InvalidTokenException ex, HttpServletRequest req) {
        ApiError body = new ApiError(HttpStatus.FORBIDDEN.value(), "Invalid Token", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
