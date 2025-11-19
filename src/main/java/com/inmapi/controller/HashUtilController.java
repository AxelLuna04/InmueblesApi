package com.inmapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hash")
@RequiredArgsConstructor
public class HashUtilController {

    private final PasswordEncoder encoder;

    /**
     * Endpoint temporal para generar BCrypt hashes para contraseñas.
     * Uso: GET /hash?pass=tuContrasenia
     */
    @GetMapping
    public ResponseEntity<String> getHash(@RequestParam("pass") String password) {
        String hashed = encoder.encode(password);
        return ResponseEntity.ok(hashed);
    }
}