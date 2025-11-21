/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.repository;

import com.inmapi.modelo.AccesoVendedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccesoVendedorRepository extends JpaRepository<AccesoVendedor, Integer> {

    Optional<AccesoVendedor> findByClienteIdAndPublicacionId(Integer idCliente, Integer idPublicacion);
    List<AccesoVendedor> findByPublicacionId(Integer idPublicacion);
    boolean existsByPublicacionId(Integer idPublicacion);
}
