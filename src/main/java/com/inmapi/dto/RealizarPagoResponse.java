/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RealizarPagoResponse {

    private boolean exito;          // true si el pago se procesó o ya tenía acceso
    private boolean yaTeniaAcceso;  // true si no se cobró porque ya estaba pagado
    private Integer idAcceso;       // id en AccesoVendedor
    private String tipoPago;        // nombre del tipo de pago
    private Double monto;           // monto registrado
    private String mensaje;         // texto para mostrar al usuario
}

