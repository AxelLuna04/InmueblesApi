/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.*;
import com.inmapi.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/publicaciones/{idPublicacion}/agenda")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping("/calendario")
    public ResponseEntity<CalendarioAgendaResponse> obtenerCalendario(
            @PathVariable Integer idPublicacion,
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        var res = citaService.obtenerCalendario(idPublicacion, anio, mes);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/horas-disponibles")
    public ResponseEntity<HorasDisponiblesResponse> obtenerHorasDisponibles(
            @PathVariable Integer idPublicacion,
            @RequestParam String fecha
    ) {
        LocalDate f = LocalDate.parse(fecha); // formato YYYY-MM-DD
        var res = citaService.obtenerHorasDisponibles(idPublicacion, f);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<AgendarCitaResponse> agendarCita(
            @PathVariable Integer idPublicacion,
            @RequestBody @Valid AgendarCitaRequest request
    ) {
        var res = citaService.agendarCita(idPublicacion, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
