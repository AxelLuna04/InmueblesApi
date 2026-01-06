package com.inmapi.service;

import com.inmapi.dto.OcupacionResponse;
import com.inmapi.repository.OcupacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogosService {

    private final OcupacionRepository ocupacionRepository;

    // Si tienes aquí lógica de TipoInmueble, déjala tal cual y agrega esto abajo:

    public List<OcupacionResponse> obtenerOcupaciones() {
        return ocupacionRepository.findAll()
                .stream()
                .map(ocupacion -> new OcupacionResponse(
                        ocupacion.getId(),
                        ocupacion.getNombre() // Asumiendo que el campo en la entidad se llama 'nombre'
                ))
                .collect(Collectors.toList());
    }
}