package com.inmapi.controller;

import com.inmapi.dto.*;
import com.inmapi.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfil;

    private boolean hasRole(String rol) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_" + rol));
    }

    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> me() {
        return ResponseEntity.ok(perfil.getPerfil());
    }

    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @PutMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PerfilResponse> cambiarFoto(@RequestPart("foto") MultipartFile foto) {
        return ResponseEntity.ok(perfil.cambiarFoto(foto));
    }

    private Object convert(Object src, Class<?> target) {
        return new com.fasterxml.jackson.databind.ObjectMapper().convertValue(src, target);
    }
}

