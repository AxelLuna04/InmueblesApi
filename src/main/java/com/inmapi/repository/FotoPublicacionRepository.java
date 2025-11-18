package com.inmapi.repository;

import com.inmapi.modelo.FotoPublicacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FotoPublicacionRepository extends JpaRepository<FotoPublicacion, Integer> {
    Optional<FotoPublicacion> findFirstByPublicacionIdAndEsPortadaTrue(Integer idPublicacion);
}

