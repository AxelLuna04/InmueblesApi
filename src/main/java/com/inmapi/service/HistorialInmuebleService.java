package com.inmapi.service;

import com.inmapi.dto.MovimientoHistorialResponse;
import com.inmapi.modelo.Cliente;
import com.inmapi.modelo.Movimiento;
import com.inmapi.modelo.Publicacion;
import com.inmapi.repository.MovimientoRepository;
import com.inmapi.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialInmuebleService {

    private final MovimientoRepository movimientos;
    private final PublicacionRepository publicaciones;

    private boolean esAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
    }

    public List<MovimientoHistorialResponse> obtenerHistorial(
            Integer idPublicacion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String tipoMovimiento
    ) {
        if (!esAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden ver el historial");
        }

        Publicacion pub = publicaciones.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        // 1. Obtener lista cruda de la BD
        List<Movimiento> lista = movimientos.findByPublicacionIdOrderByFechaDesc(pub.getId());

        // 2. Filtros básicos (Opcional: Si quieres que la BD filtre, podrías cambiar el query, 
        // pero filtrar aquí en memoria está bien para listas cortas).
        String tipoFiltro = (tipoMovimiento == null || tipoMovimiento.isBlank())
                ? null
                : tipoMovimiento.toUpperCase(Locale.ROOT);

        return lista.stream()
                .filter(m -> fechaInicio == null || !m.getFecha().isBefore(fechaInicio))
                .filter(m -> fechaFin == null || !m.getFecha().isAfter(fechaFin))
                .filter(m -> tipoFiltro == null || m.getTipoMovimiento().equalsIgnoreCase(tipoFiltro))
                .map(this::mapearADto) // Conversión directa 1 a 1
                .collect(Collectors.toList());
    }

    // Método simple para convertir de Entidad -> DTO
    private MovimientoHistorialResponse mapearADto(Movimiento m) {
        MovimientoHistorialResponse res = new MovimientoHistorialResponse();
        res.setIdMovimiento(m.getId());
        res.setTipoMovimiento(m.getTipoMovimiento()); // Tal cual sale de la BD
        res.setFecha(m.getFecha()); // La fecha exacta del evento

        Cliente c = m.getArrendador();
        if (c != null) {
            res.setNombreCliente(c.getNombreCompleto());
        }

        // Precio solo relevante si es VENTA o RENTA, lo tomamos de la publicación actual
        // (Nota: Esto muestra el precio ACTUAL, no el histórico, a menos que guardes precio en Movimiento)
        if (esTipoConPrecio(m.getTipoMovimiento())) {
            res.setPrecio(m.getPublicacion().getPrecio());
        }

        // Generar una descripción sencilla
        res.setDescripcion(generarDescripcion(m));

        return res;
    }

    private boolean esTipoConPrecio(String tipo) {
        if (tipo == null) {
            return false;
        }
        String t = tipo.toUpperCase();
        return t.contains("VENTA") || t.contains("RENTA");
    }

    private String generarDescripcion(Movimiento m) {
        String tipo = (m.getTipoMovimiento() != null) ? m.getTipoMovimiento().toUpperCase() : "MOVIMIENTO";
        String cliente = (m.getArrendador() != null) ? " - Cliente: " + m.getArrendador().getNombreCompleto() : "";

        switch (tipo) {
            case "CREACION":
                return "Publicación creada";
            case "EDICION":
                return "Publicación editada";
            case "APROBACION":
                return "Publicación aprobada por admin";
            case "RECHAZADO":
                return "Publicación rechazada";
            case "RENTADO":
                return "Inmueble rentado" + cliente;
            case "FIN_RENTA":
                return "Fin de periodo de renta" + cliente;
            case "VENTA":
                return "Inmueble vendido" + cliente;
            default:
                return "Movimiento: " + tipo + cliente;
        }
    }
}
