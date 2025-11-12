package com.inmapi.controller;

import com.inmapi.dto.ChangeEmailRequest;
import com.inmapi.dto.ChangePasswordRequest;
import com.inmapi.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuenta;

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
        cuenta.cambiarContrasenia(req);
        return ResponseEntity.ok().body(
                java.util.Map.of("mensaje", "Contraseña actualizada")
        );
    }

    @PostMapping("/email")
    public ResponseEntity<?> requestEmailChange(@RequestBody @Valid ChangeEmailRequest req) {
        cuenta.solicitarCambioCorreo(req);
        return ResponseEntity.ok().body(
                java.util.Map.of("mensaje", "Revisa el nuevo correo para confirmar el cambio")
        );
    }
}

