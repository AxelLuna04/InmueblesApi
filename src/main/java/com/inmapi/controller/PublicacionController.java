package com.inmapi.controller;

//import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmapi.dto.CrearPublicacionRequest;
import com.inmapi.dto.CrearPublicacionResponse;
import com.inmapi.dto.PublicacionCard;
import com.inmapi.dto.PublicacionDetalle;
import com.inmapi.dto.PublicacionFiltro;
import com.inmapi.dto.UpdatePublicacionRequest;
import com.inmapi.service.PublicacionQueryService;
import com.inmapi.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicaciones;
    private final PublicacionQueryService service;
    //private final ObjectMapper om = new ObjectMapper();

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CrearPublicacionResponse> crear(
            @RequestPart("datos") @Valid CrearPublicacionRequest datos,
            @RequestPart("fotos") MultipartFile[] fotos
    ) {
        var res = publicaciones.crear(datos, java.util.Arrays.asList(fotos));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
    
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CrearPublicacionResponse> actualizar(
        @PathVariable Integer id,
        @RequestPart("datos") UpdatePublicacionRequest datos,
        @RequestPart(value = "fotosNuevas", required = false) MultipartFile[] fotosNuevas
    ) {
      var res = publicaciones.actualizar(id,
          datos,
          fotosNuevas == null ? java.util.List.of() : java.util.Arrays.asList(fotosNuevas));
      return ResponseEntity.ok(res);
    }


    // Esto es por si el metodo de arriba falla debido a un bug que se tiene al aplicar @Valid a un @RequestPart
    /*public ResponseEntity<CrearPublicacionResponse> crear(
            @RequestPart("datos") String datosJson,
            @RequestPart("fotos") MultipartFile[] fotos
    ) {
        var datos = om.readValue(datosJson, CrearPublicacionRequest.class);
        var res = publicaciones.crear(datos, Arrays.asList(fotos));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }*/
    // Lista paginada (cards)
    @GetMapping
    public ResponseEntity<Page<PublicacionCard>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, name = "tipo") Integer idTipoInmueble,
            @RequestParam(required = false, name = "pmin") Double precioMin,
            @RequestParam(required = false, name = "pmax") Double precioMax,
            @RequestParam(required = false, name = "habMin") Integer habMin,
            @RequestParam(required = false, name = "banosMin") Integer banosMin,
            @RequestParam(required = false, name = "excMin") Integer excMin,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, name = "ubi") String ubicacion,
            @RequestParam(required = false, name = "caracts") String caracteristicasCsv
    ) {
        var f = new PublicacionFiltro();
        f.setIdTipoInmueble(idTipoInmueble);
        f.setPrecioMin(precioMin);
        f.setPrecioMax(precioMax);
        f.setHabMin(habMin);
        f.setBanosMin(banosMin);
        f.setExcusadosMin(excMin);
        f.setQ(q);
        f.setUbicacion(ubicacion);
        f.setCaracteristicas(caracteristicasCsv);
        return ResponseEntity.ok(service.listar(f, page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionDetalle> detalle(@PathVariable Integer id) {
        return ResponseEntity.ok(service.detalle(id));
    }

    @GetMapping("/para-ti")
    public ResponseEntity<Page<PublicacionCard>> paraTi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(service.paraTi(page, size));
    }
}

