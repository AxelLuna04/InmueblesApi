/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.dto;

import java.util.List;
import lombok.Data;

/**
 *
 * @author HP
 */
@Data
public class CalendarioAgendaResponse {
    private int anio;
    private int mes;
    private List<DiaCalendarioAgendaResponse> dias;
}