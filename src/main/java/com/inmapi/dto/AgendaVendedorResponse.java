package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AgendaVendedorResponse {
    private Integer idCita;
    private LocalDate fecha;
    private LocalTime hora;
    private String nombrePropiedad;
    private String nombreCliente;
    private String telefonoCliente; 
}