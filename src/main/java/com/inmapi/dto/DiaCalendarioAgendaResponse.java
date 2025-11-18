/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DiaCalendarioAgendaResponse {
    private LocalDate fecha;
    private boolean habilitado; // el vendedor atiende este día
    private boolean lleno;      // día marcado como lleno (rojo)
}


