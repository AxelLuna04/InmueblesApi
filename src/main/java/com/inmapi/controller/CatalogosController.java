package com.inmapi.controller;

import com.inmapi.dto.CaracteristicaDTO;
import com.inmapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogosController {

  private final TipoInmuebleRepository tipos;
  private final ListaCaracteristicasRepository listas;

  @GetMapping("/tipos-inmueble")
  public ResponseEntity<?> tiposInmueble() {
    return ResponseEntity.ok(tipos.findAll());
  }

  @GetMapping("/tipos-inmueble/{id}/caracteristicas")
  public ResponseEntity<?> caracteristicasPorTipo(@PathVariable Integer id) {

    var lista = listas.findByTipoInmuebleId(id)
        .stream()
        .map(listaCaracteristica -> new CaracteristicaDTO(listaCaracteristica.getCaracteristica()))
        .toList();
        
    return ResponseEntity.ok(lista);
  }
}
