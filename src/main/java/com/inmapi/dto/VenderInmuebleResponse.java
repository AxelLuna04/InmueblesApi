/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VenderInmuebleResponse {

    private Integer idPublicacion;
    private String estadoPublicacion; // por ejemplo "VENDIDA"
    private Integer idMovimiento;     // registro en la tabla Movimiento
    private Integer idContrato;       // null si no se subió documento
    private String mensaje;           // texto para mostrar al usuario
}

