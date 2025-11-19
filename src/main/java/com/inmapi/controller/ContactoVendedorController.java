/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.ContactoVendedorResponse;
import com.inmapi.service.ContactoVendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/publicaciones/{idPublicacion}")
@RequiredArgsConstructor
public class ContactoVendedorController {

    private final ContactoVendedorService contactoService;

    @GetMapping("/contacto")
    public ResponseEntity<ContactoVendedorResponse> obtenerContacto(
            @PathVariable Integer idPublicacion
    ) {
        var res = contactoService.obtenerContactoVendedor(idPublicacion);
        return ResponseEntity.ok(res);
    }
}
