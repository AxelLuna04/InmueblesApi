/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.inmapi.repository;

import com.inmapi.modelo.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Integer> {

    List<Agenda> findByVendedorIdAndFechaSeleccionada(Integer idVendedor, LocalDate fecha);

    List<Agenda> findByVendedorIdAndFechaSeleccionadaGreaterThanEqualOrderByFechaSeleccionadaAscHoraSeleccionadaAsc(
            Integer idVendedor, 
            LocalDate fecha
    );
    
    boolean existsByVendedorIdAndFechaSeleccionadaAndHoraSeleccionada(
            Integer idVendedor,
            LocalDate fecha,
            LocalTime hora
    );
}

