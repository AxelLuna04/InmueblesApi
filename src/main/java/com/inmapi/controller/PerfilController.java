package com.inmapi.controller;

import com.inmapi.dto.*;
import com.inmapi.service.PerfilService;
import com.inmapi.service.CuentaService; // <-- 1. AÑADIR DEPENDENCIA
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/me") // <-- RUTA BASE (¡perfecta!)
@RequiredArgsConstructor
public class PerfilController {

    // --- 2. AÑADIR AMBOS SERVICIOS ---
    private final PerfilService perfil;
    private final CuentaService cuenta;

    // === MÉTODOS DE PERFIL (VER Y ACTUALIZAR) ===

    @GetMapping
    public ResponseEntity<PerfilResponse> me() {
        return ResponseEntity.ok(perfil.getPerfil());
    }

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerfilResponse> patchMe(@RequestBody @Valid Object body) {
        if (hasRole("CLIENTE")) {
            var dto = (UpdatePerfilClienteRequest) convert(body, UpdatePerfilClienteRequest.class);
            return ResponseEntity.ok(perfil.patchCliente(dto));
        } else if (hasRole("VENDEDOR")) {
            var dto = (UpdatePerfilVendedorRequest) convert(body, UpdatePerfilVendedorRequest.class);
            return ResponseEntity.ok(perfil.patchVendedor(dto));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PerfilResponse> cambiarFoto(@RequestPart("foto") MultipartFile foto) {
        return ResponseEntity.ok(perfil.cambiarFoto(foto));
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
        cuenta.cambiarContrasenia(req);
        return ResponseEntity.ok(java.util.Map.of("mensaje", "Contraseña actualizada"));
    }

    @PostMapping("/email")
    public ResponseEntity<?> requestEmailChange(@RequestBody @Valid ChangeEmailRequest req) {
        cuenta.solicitarCambioCorreo(req);
        return ResponseEntity.ok(java.util.Map.of("mensaje", "Revisa el nuevo correo para confirmar el cambio"));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteMyAccount(@RequestBody @Valid DeleteAccountRequest req) {
        cuenta.eliminarCuenta(req.getContraseniaActual());
        return ResponseEntity.ok(java.util.Map.of("mensaje", "Cuenta eliminada"));
    }

    private boolean hasRole(String rol) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_" + rol));
    }

    private Object convert(Object src, Class<?> target) {
        return new com.fasterxml.jackson.databind.ObjectMapper().convertValue(src, target);
    }
}
