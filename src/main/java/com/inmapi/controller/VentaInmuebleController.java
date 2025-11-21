/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.controller;

import com.inmapi.dto.VenderInmuebleRequest;
import com.inmapi.dto.VenderInmuebleResponse;
import com.inmapi.service.VentaInmuebleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/publicaciones/{idPublicacion}")
@RequiredArgsConstructor
public class VentaInmuebleController {

    private final VentaInmuebleService ventaService;

    @PatchMapping(value = "/vender", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VenderInmuebleResponse> venderInmueble(
            @PathVariable Integer idPublicacion,
            @RequestPart("datosVenta") @Valid VenderInmuebleRequest datos,
            @RequestPart(value = "documentoVenta", required = false) MultipartFile documentoVenta
    ) {
        var res = ventaService.venderInmueble(idPublicacion, datos, documentoVenta);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/interesados")
    public ResponseEntity<java.util.List<com.inmapi.dto.InteresadoResponse>> listarInteresados(
            @PathVariable Integer idPublicacion
    ) {
        var res = ventaService.listarInteresados(idPublicacion);
        return ResponseEntity.ok(res);
    }
}
