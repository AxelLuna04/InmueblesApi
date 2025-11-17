/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ConfigurarAgendaRequest {

    @NotNull private Boolean lunes;
    @NotNull private Boolean martes;
    @NotNull private Boolean miercoles;
    @NotNull private Boolean jueves;
    @NotNull private Boolean viernes;
    @NotNull private Boolean sabado;
    @NotNull private Boolean domingo;

    @NotNull
    private LocalTime horarioAtencionInicio;

    @NotNull
    private LocalTime horarioAtencionFin;

    @NotNull
    @Positive
    private Double duracionVisita; 
}

