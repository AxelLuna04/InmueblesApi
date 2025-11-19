package com.inmapi.controller;

import com.inmapi.dto.*;
import com.inmapi.service.AdminPublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/publicaciones")
@RequiredArgsConstructor
public class AdminPublicacionController {

    private final AdminPublicacionService admin;

    @GetMapping
    public ResponseEntity<Page<ModeracionCard>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer tipo,
            @RequestParam(required = false) String q
    ) {
        ModeracionFiltro f = new ModeracionFiltro();
        f.setEstado(estado);
        f.setTipo(tipo);
        f.setQ(q);
        return ResponseEntity.ok(admin.listar(f, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeracionDetalle> detalle(@PathVariable Integer id) {
        return ResponseEntity.ok(admin.detalle(id));
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<ModeracionResponse> aprobar(@PathVariable Integer id) {
        return ResponseEntity.ok(admin.aprobar(id));
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<ModeracionResponse> rechazar(@PathVariable Integer id,
            @RequestBody @Valid RechazoRequest body) {
        return ResponseEntity.ok(admin.rechazar(id, body.getMotivo()));
    }
}

