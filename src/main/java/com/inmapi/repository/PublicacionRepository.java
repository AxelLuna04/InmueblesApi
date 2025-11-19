package com.inmapi.repository;

import com.inmapi.modelo.Publicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer>, JpaSpecificationExecutor<Publicacion> {
    Page<com.inmapi.modelo.Publicacion> findByVendedorCorreoOrderByCreadoEnDesc(String correo, Pageable pageable);
}

