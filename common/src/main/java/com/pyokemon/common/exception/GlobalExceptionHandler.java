package com.pyokemon.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.pyokemon.common.dto.ResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ResponseDto<Void>> handleBusinessException(BusinessException e) {
    log.error("Business exception occurred: {}", e.getMessage(), e);

    ResponseDto<Void> response = ResponseDto.error(e.getMessage(), e.getErrorCode());
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ResponseDto<Void>> handleAuthenticationException(AuthenticationException e) {
    log.error("Authentication exception occurred: {}", e.getMessage(), e);

    ResponseDto<Void> response = ResponseDto.error("인증이 필요합니다.", "ACCESS_DENIED");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ResponseDto<Void>> handleAccessDeniedException(AccessDeniedException e) {
    log.error("Access denied exception occurred: {}", e.getMessage(), e);

    ResponseDto<Void> response = ResponseDto.error("접근 권한이 없습니다.", "PERMISSION_DENIED");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationException(
      MethodArgumentNotValidException e) {
    log.error("Validation exception occurred", e);

    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    ResponseDto<Map<String, String>> response = ResponseDto.error("유효성 검증 실패", "VALIDATION_ERROR");
    response = ResponseDto.<Map<String, String>>builder().success(false).message("유효성 검증 실패")
        .errorCode("VALIDATION_ERROR").data(errors).build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(InvalidFormatException.class)
  public ResponseEntity<ResponseDto<Map<String, String>>> handleInvalidFormatException(InvalidFormatException e) {
    log.error("Invalid format exception occurred: {}", e.getMessage(), e);
    
    Map<String, String> errors = new HashMap<>();
    String fieldName = e.getPath().isEmpty() ? "unknown" : e.getPath().get(0).getFieldName();
    String errorMessage = String.format("잘못된 형식의 데이터입니다. 입력값: '%s', 필요한 타입: %s", 
        e.getValue(), e.getTargetType().getSimpleName());
    errors.put(fieldName, errorMessage);
    
    ResponseDto<Map<String, String>> response = ResponseDto.<Map<String, String>>builder()
        .success(false)
        .message("데이터 형식 오류")
        .errorCode("INVALID_FORMAT")
        .data(errors)
        .build();
        
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseDto<Void>> handleException(Exception e) {
    log.error("Unexpected exception occurred", e);

    ResponseDto<Void> response = ResponseDto.error("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
