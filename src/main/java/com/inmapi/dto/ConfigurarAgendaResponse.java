/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ConfigurarAgendaResponse {

    private Boolean lunes;
    private Boolean martes;
    private Boolean miercoles;
    private Boolean jueves;
    private Boolean viernes;
    private Boolean sabado;
    private Boolean domingo;

    private LocalTime horarioAtencionInicio;
    private LocalTime horarioAtencionFin;
    private Double duracionVisita;
}

