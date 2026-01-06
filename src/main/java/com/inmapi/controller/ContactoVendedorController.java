/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.ContactoVendedorResponse;
import com.inmapi.service.ContactoVendedorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1") // 1. Cambiamos la ruta base para que sea más general
@RequiredArgsConstructor
public class ContactoVendedorController {

    private final ContactoVendedorService contactoService;

    // Endpoint existente (ajustamos la ruta aquí)
    @GetMapping("/publicaciones/{idPublicacion}/contacto")
    public ResponseEntity<ContactoVendedorResponse> obtenerContacto(
            @PathVariable Integer idPublicacion
    ) {
        var res = contactoService.obtenerContactoVendedor(idPublicacion);
        return ResponseEntity.ok(res);
    }

    // 2. NUEVO Endpoint para la lista
    @GetMapping("/mis-contactos-desbloqueados")
    public ResponseEntity<List<ContactoVendedorResponse>> obtenerMisContactos() {
        var res = contactoService.listarVendedoresDesbloqueados();
        return ResponseEntity.ok(res);
    }
}
