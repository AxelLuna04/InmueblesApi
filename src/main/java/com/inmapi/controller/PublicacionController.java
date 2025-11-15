package com.inmapi.controller;

//import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmapi.dto.CrearPublicacionRequest;
import com.inmapi.dto.CrearPublicacionResponse;
import com.inmapi.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicaciones;
    //private final ObjectMapper om = new ObjectMapper();

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CrearPublicacionResponse> crear(
            @RequestPart("datos") @Valid CrearPublicacionRequest datos,
            @RequestPart("fotos") MultipartFile[] fotos
    ) {
        var res = publicaciones.crear(datos, java.util.Arrays.asList(fotos));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
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

}

