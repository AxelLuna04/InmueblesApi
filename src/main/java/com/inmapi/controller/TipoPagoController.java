/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.TipoPagoResponse;
import com.inmapi.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipos-pago")
@RequiredArgsConstructor
public class TipoPagoController {

    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<TipoPagoResponse>> listarTiposPago() {
        var lista = pagoService.obtenerTiposPago();
        return ResponseEntity.ok(lista);
    }
}
