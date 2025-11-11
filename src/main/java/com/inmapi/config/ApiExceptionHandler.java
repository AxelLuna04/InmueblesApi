package com.inmapi.config;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public Map<String, Object> validation(MethodArgumentNotValidException ex) {
    var fields = ex.getBindingResult().getFieldErrors().stream()
      .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (a,b)->a));
    return Map.of("status", 422, "error", "ValidationError", "fields", fields);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> status(ResponseStatusException ex) {
    var body = Map.of("status", ex.getStatusCode().value(), "error", ex.getReason());
    return ResponseEntity.status(ex.getStatusCode()).body(body);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, Object> other(Exception ex) {
    ex.printStackTrace();
    return Map.of("status", 500, "error", "ServerError");
  }
}
