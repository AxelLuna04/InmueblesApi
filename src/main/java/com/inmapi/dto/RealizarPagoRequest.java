/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RealizarPagoRequest {

    @NotNull
    private Integer idTipoPago;

    @NotNull
    @Positive
    private Double monto;

    /**
     * Datos de pago simulados, por ejemplo:
     * "VISA **** 4242, vencimiento 12/28"
     * "paypal: correo@ejemplo.com"
     */
    @NotBlank
    @Size(max = 255)
    private String datosSimulados;
}

