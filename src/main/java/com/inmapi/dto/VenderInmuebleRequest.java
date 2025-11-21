/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VenderInmuebleRequest {

    // Cliente que compra (interesado seleccionado en la tabla)
    @NotNull
    private Integer idClienteComprador;

    // Fecha de la venta
    @NotNull
    private LocalDate fechaVenta;
}

