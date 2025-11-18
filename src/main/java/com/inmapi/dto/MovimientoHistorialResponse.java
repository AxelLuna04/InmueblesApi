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
    private String tipoMovimiento; // CREACION, APROBACION, RENTA, EDICION, VENTA, etc.

    // Para todos los movimientos:
    // - eventos simples (creación, edición, etc.): fechaFin = null
    // - renta: fechaInicio = inicio renta, fechaFin = fin renta
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String nombreCliente; // solo renta / venta
    private Double precio;        // solo renta / venta

    // Texto listo para mostrar
    private String descripcion;
}

