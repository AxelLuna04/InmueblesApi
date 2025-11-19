package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MisPubCard {

    private Integer id;
    private String titulo;
    private String estado;
    private Double precio;
    private String tipoInmueble;
    private String portada;
    private LocalDateTime creadoEn;
}
