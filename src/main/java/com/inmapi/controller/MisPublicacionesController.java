package com.inmapi.controller;

import com.inmapi.dto.MisPubCard;
import com.inmapi.dto.PublicacionDetalle;
import com.inmapi.service.MisPublicacionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mis-publicaciones")
@RequiredArgsConstructor
public class MisPublicacionesController {

  private final MisPublicacionesService service;

  @GetMapping
  public ResponseEntity<Page<MisPubCard>> listar(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size
  ) {
    return ResponseEntity.ok(service.listar(page, size));
  }

  @GetMapping("/{id}")
  public ResponseEntity<PublicacionDetalle> obtener(@PathVariable Integer id) {
    return ResponseEntity.ok(service.obtenerDetalleMio(id));
  }
}

