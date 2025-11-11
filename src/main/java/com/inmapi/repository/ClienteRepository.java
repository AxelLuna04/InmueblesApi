package com.inmapi.repository;

import com.inmapi.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
  boolean existsByCorreo(String correo);
  Optional<Cliente> findByTokenVerificacion(String token);
  Optional<Cliente> findByCorreo(String correo);
}

