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

    public List<OcupacionResponse> obtenerOcupaciones() {
        return ocupacionRepository.findAll()
                .stream()
                .map(ocupacion -> new OcupacionResponse(
                        ocupacion.getId(),
                        ocupacion.getNombre()
                ))
                .collect(Collectors.toList());
    }
}