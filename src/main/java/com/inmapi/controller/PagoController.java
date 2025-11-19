/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.RealizarPagoRequest;
import com.inmapi.dto.RealizarPagoResponse;
import com.inmapi.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/publicaciones/{idPublicacion}")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/pagar-acceso")
    public ResponseEntity<RealizarPagoResponse> pagarAcceso(
            @PathVariable Integer idPublicacion,
            @RequestBody @Valid RealizarPagoRequest request
    ) {
        var res = pagoService.pagarAcceso(idPublicacion, request);

        // Si ya tenía acceso → 200; si se acaba de crear acceso → 201
        HttpStatus status = res.isYaTeniaAcceso() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(res);
    }
}
