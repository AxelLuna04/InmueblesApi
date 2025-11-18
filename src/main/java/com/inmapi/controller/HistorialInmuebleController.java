/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.MovimientoHistorialResponse;
import com.inmapi.service.HistorialInmuebleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/publicaciones/{idPublicacion}/historial")
@RequiredArgsConstructor
public class HistorialInmuebleController {

    private final HistorialInmuebleService historialService;

    @GetMapping
    public ResponseEntity<List<MovimientoHistorialResponse>> obtenerHistorial(
            @PathVariable Integer idPublicacion,
            @RequestParam(required = false) String tipoMovimiento,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin
    ) {

        LocalDate fi = (fechaInicio != null && !fechaInicio.isBlank())
                ? LocalDate.parse(fechaInicio)   // formato YYYY-MM-DD
                : null;

        LocalDate ff = (fechaFin != null && !fechaFin.isBlank())
                ? LocalDate.parse(fechaFin)
                : null;

        var res = historialService.obtenerHistorial(idPublicacion, fi, ff, tipoMovimiento);
        return ResponseEntity.ok(res);
    }
}
