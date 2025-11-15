package com.inmapi.repository;

import com.inmapi.modelo.ListaCaracteristicas;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListaCaracteristicasRepository extends JpaRepository<ListaCaracteristicas, Integer> {
  List<ListaCaracteristicas> findByTipoInmuebleId(Integer idTipo);
}

