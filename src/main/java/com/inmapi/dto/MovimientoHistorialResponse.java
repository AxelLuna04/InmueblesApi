package com.inmapi.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MovimientoHistorialResponse {

    private Integer idMovimiento;
    private String tipoMovimiento; 
    
    private LocalDate fecha; 

    private String nombreCliente; 
    private Double precio;        
    private String descripcion;
}