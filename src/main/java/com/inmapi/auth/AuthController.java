package com.inmapi.auth;

import com.inmapi.dto.LoginRequest;
import com.inmapi.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService service;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    return ResponseEntity.ok(service.login(req.getCorreo(), req.getContrasenia()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String refresh = authHeader.substring(7);
    return ResponseEntity.ok(service.refresh(refresh));
  }
}

