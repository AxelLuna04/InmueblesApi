package com.inmapi.repository;

import com.inmapi.modelo.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {
  boolean existsByCorreo(String correo);
  Optional<Vendedor> findByTokenVerificacion(String token);
}

