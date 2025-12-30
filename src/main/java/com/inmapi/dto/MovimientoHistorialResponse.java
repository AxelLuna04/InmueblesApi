/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MovimientoHistorialResponse {

    private Integer idMovimiento;
    private String tipoMovimiento; 

   
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String nombreCliente; 
    private Double precio;        

    // Texto listo para mostrar
    private String descripcion;
}

