/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    // ==== seguridad: ajusta el rol al que usen ustedes ====
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

        List<Movimiento> lista = movimientos.findByPublicacionIdOrderByFechaDesc(pub.getId());
        Collections.reverse(lista); // ahora están ASC

        List<MovimientoHistorialResponse> agregados = agruparMovimientos(lista);

        String tipoFiltro = (tipoMovimiento == null || tipoMovimiento.isBlank())
                ? null
                : tipoMovimiento.toUpperCase(Locale.ROOT);

        return agregados.stream()
                .filter(m -> fechaInicio == null || !m.getFechaInicio().isBefore(fechaInicio))
                .filter(m -> fechaFin == null || !m.getFechaInicio().isAfter(fechaFin))
                .filter(m -> tipoFiltro == null || m.getTipoMovimiento().equalsIgnoreCase(tipoFiltro))
                // devolver ordenados de más nuevo a más viejo
                .sorted(Comparator.comparing(MovimientoHistorialResponse::getFechaInicio).reversed())
                .collect(Collectors.toList());
    }


    private List<MovimientoHistorialResponse> agruparMovimientos(List<Movimiento> movimientosOrdenAsc) {
        List<MovimientoHistorialResponse> resultado = new ArrayList<>();

        Movimiento rentaAbierta = null;

        for (Movimiento m : movimientosOrdenAsc) {
            String tipo = (m.getTipoMovimiento() == null)
                    ? ""
                    : m.getTipoMovimiento().toUpperCase(Locale.ROOT);

            switch (tipo) {
                case "RENTADO":
                    // iniciamos una renta; asumimos que solo hay una abierta a la vez
                    rentaAbierta = m;
                    break;

                case "FIN_RENTA":
                case "FIN_DE_RENTA":
                    if (rentaAbierta != null
                            && mismoCliente(rentaAbierta.getArrendador(), m.getArrendador())) {

                        resultado.add(crearMovimientoRenta(rentaAbierta, m));
                        rentaAbierta = null;
                    } else {
                        resultado.add(crearMovimientoSimple(m));
                    }
                    break;

                default:
                    resultado.add(crearMovimientoSimple(m));
            }
        }

        if (rentaAbierta != null) {
            resultado.add(crearMovimientoRenta(rentaAbierta, null));
        }

        return resultado;
    }

    private boolean mismoCliente(Cliente c1, Cliente c2) {
        if (c1 == null || c2 == null) return false;
        return Objects.equals(c1.getId(), c2.getId());
    }


    private MovimientoHistorialResponse crearMovimientoSimple(Movimiento m) {
        MovimientoHistorialResponse res = new MovimientoHistorialResponse();
        res.setIdMovimiento(m.getId());
        res.setTipoMovimiento(normalizarTipoSimple(m.getTipoMovimiento()));
        res.setFechaInicio(m.getFecha());
        res.setFechaFin(null);

        Cliente c = m.getArrendador();
        if (c != null) {
            res.setNombreCliente(c.getNombreCompleto());
        }

        String tipo = res.getTipoMovimiento().toUpperCase(Locale.ROOT);
        Double precio = null;
        if (tipo.equals("VENTA") || tipo.equals("VENDIDO")) {
            precio = m.getPublicacion().getPrecio();
        }
        res.setPrecio(precio);

        String desc;
        switch (tipo) {
            case "CREACION":
                desc = "Inmueble creado";
                break;
            case "APROBACION":
            case "APROBADO":
                desc = "Inmueble aprobado";
                break;
            case "EDICION":
            case "EDITADO":
                desc = "Inmueble editado";
                break;
            case "VENTA":
            case "VENDIDO":
                desc = (c != null)
                        ? "Inmueble vendido a " + c.getNombreCompleto() + textoPrecio(precio)
                        : "Inmueble vendido" + textoPrecio(precio);
                break;
            default:
                desc = (tipo.isEmpty()) ? "Movimiento" : ("Movimiento: " + tipo);
        }
        res.setDescripcion(desc);

        return res;
    }

    private MovimientoHistorialResponse crearMovimientoRenta(Movimiento rentado, Movimiento finRenta) {
        MovimientoHistorialResponse res = new MovimientoHistorialResponse();
        res.setIdMovimiento(rentado.getId()); 

        res.setTipoMovimiento("RENTA"); 

        res.setFechaInicio(rentado.getFecha());
        res.setFechaFin(finRenta != null ? finRenta.getFecha() : null);

        Cliente c = rentado.getArrendador();
        if (c != null) {
            res.setNombreCliente(c.getNombreCompleto());
        }

        Double precio = rentado.getPublicacion().getPrecio(); 
        res.setPrecio(precio);

        String rango = (finRenta != null)
                ? "del " + rentado.getFecha() + " al " + finRenta.getFecha()
                : "a partir del " + rentado.getFecha();

        String desc = (c != null)
                ? "Renta de " + c.getNombreCompleto() + " " + rango + textoPrecio(precio)
                : "Renta " + rango + textoPrecio(precio);

        res.setDescripcion(desc);

        return res;
    }

    private String textoPrecio(Double precio) {
        if (precio == null) return "";
        return " por $" + precio;
    }

    private String normalizarTipoSimple(String tipoDb) {
        if (tipoDb == null) return "";
        String t = tipoDb.toUpperCase(Locale.ROOT);
        switch (t) {
            case "RENTADO":
            case "FIN_RENTA":
            case "FIN_DE_RENTA":
                return "RENTA";
            default:
                return t;
        }
    }
}
