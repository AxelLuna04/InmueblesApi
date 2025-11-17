/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.ConfigurarAgendaRequest;
import com.inmapi.dto.ConfigurarAgendaResponse;
import com.inmapi.service.AgendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService agendaService;

    @GetMapping
    public ResponseEntity<ConfigurarAgendaResponse> obtenerAgenda() {
        var res = agendaService.obtenerAgenda();
        return ResponseEntity.ok(res);
    }

    @PutMapping
    public ResponseEntity<ConfigurarAgendaResponse> guardarAgenda(
            @RequestBody @Valid ConfigurarAgendaRequest request
    ) {
        var res = agendaService.guardarAgenda(request);
        return ResponseEntity.ok(res);
    }
}
