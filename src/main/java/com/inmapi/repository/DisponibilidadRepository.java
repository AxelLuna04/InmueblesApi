/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.inmapi.repository;

import com.inmapi.modelo.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {

    Optional<Disponibilidad> findByVendedorId(Integer idVendedor);
}

