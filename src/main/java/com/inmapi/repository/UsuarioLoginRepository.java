package com.inmapi.repository;

import com.inmapi.modelo.UsuarioLoginView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioLoginRepository extends JpaRepository<UsuarioLoginView, Integer> {
    Optional<UsuarioLoginView> findByCorreo(String correo);
}
