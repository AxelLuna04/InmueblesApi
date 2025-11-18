/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.inmapi.repository;

import com.inmapi.modelo.DiaOcupado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DiaOcupadoRepository extends JpaRepository<DiaOcupado, Integer> {

    boolean existsByVendedorIdAndFecha(Integer idVendedor, LocalDate fecha);
}

