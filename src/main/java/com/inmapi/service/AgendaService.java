/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.ConfigurarAgendaRequest;
import com.inmapi.dto.ConfigurarAgendaResponse;
import com.inmapi.modelo.Disponibilidad;
import com.inmapi.modelo.Vendedor;
import com.inmapi.repository.DisponibilidadRepository;
import com.inmapi.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AgendaService {

    private final VendedorRepository vendedores;
    private final DisponibilidadRepository disponibilidades;

    private String emailActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (String) a.getPrincipal();
    }

    private boolean esVendedor() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_VENDEDOR"));
    }

    private Vendedor vendedorActual() {
        var correo = emailActual();
        return vendedores.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
    }

    public ConfigurarAgendaResponse obtenerAgenda() {
        if (!esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo vendedores pueden ver su agenda");
        }

        Vendedor vendedor = vendedorActual();

        Disponibilidad d = disponibilidades.findByVendedorId(vendedor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agenda no configurada"));

        return mapearAResponse(d);
    }

    @Transactional
    public ConfigurarAgendaResponse guardarAgenda(ConfigurarAgendaRequest req) {
        if (!esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo vendedores pueden configurar su agenda");
        }

        // Validaciones de negocio
        if (!hayAlMenosUnDia(req)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Debes seleccionar al menos un día disponible");
        }

        if (!req.getHorarioAtencionInicio().isBefore(req.getHorarioAtencionFin())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "La hora de inicio debe ser menor a la de fin");
        }

        if (req.getDuracionVisita() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "La duración de la visita debe ser mayor a 0");
        }

        Vendedor vendedor = vendedorActual();

        // Buscar si ya tiene una disponibilidad, si no crear una nueva
        Disponibilidad disp = disponibilidades.findByVendedorId(vendedor.getId())
                .orElseGet(() -> {
                    Disponibilidad nueva = new Disponibilidad();
                    nueva.setVendedor(vendedor);
                    return nueva;
                });

        String diasDisponibles = diasToString(req);
        disp.setDiasDisponibles(diasDisponibles);
        // si quieres usar diasNoDisponibles, puedes guardarlo como el complemento
        disp.setDiasNoDisponibles(invertirDias(diasDisponibles));

        disp.setHorarioAtencionInicio(req.getHorarioAtencionInicio());
        disp.setHorarioAtencionFin(req.getHorarioAtencionFin());
        disp.setDuracionVisita(req.getDuracionVisita());

        disp = disponibilidades.save(disp);

        return mapearAResponse(disp);
    }

    // ---- helpers para mapear días y respuesta ----

    private boolean hayAlMenosUnDia(ConfigurarAgendaRequest r) {
        return Boolean.TRUE.equals(r.getLunes())
                || Boolean.TRUE.equals(r.getMartes())
                || Boolean.TRUE.equals(r.getMiercoles())
                || Boolean.TRUE.equals(r.getJueves())
                || Boolean.TRUE.equals(r.getViernes())
                || Boolean.TRUE.equals(r.getSabado())
                || Boolean.TRUE.equals(r.getDomingo());
    }

    // convierte booleans a "1111100"
    private String diasToString(ConfigurarAgendaRequest r) {
        StringBuilder sb = new StringBuilder(7);
        sb.append(Boolean.TRUE.equals(r.getLunes()) ? '1' : '0');
        sb.append(Boolean.TRUE.equals(r.getMartes()) ? '1' : '0');
        sb.append(Boolean.TRUE.equals(r.getMiercoles()) ? '1' : '0');
        sb.append(Boolean.TRUE.equals(r.getJueves()) ? '1' : '0');
        sb.append(Boolean.TRUE.equals(r.getViernes()) ? '1' : '0');
        sb.append(Boolean.TRUE.equals(r.getSabado()) ? '1' : '0');
        sb.append(Boolean.TRUE.equals(r.getDomingo()) ? '1' : '0');
        return sb.toString();
    }

    private String invertirDias(String dias) {
        if (dias == null || dias.length() != 7) return null;
        StringBuilder sb = new StringBuilder(7);
        for (int i = 0; i < 7; i++) {
            char c = dias.charAt(i);
            sb.append(c == '1' ? '0' : '1');
        }
        return sb.toString();
    }

    private ConfigurarAgendaResponse mapearAResponse(Disponibilidad d) {
        ConfigurarAgendaResponse res = new ConfigurarAgendaResponse();
        String dias = d.getDiasDisponibles();
        if (dias == null || dias.length() != 7) {
            dias = "0000000";
        }

        res.setLunes(dias.charAt(0) == '1');
        res.setMartes(dias.charAt(1) == '1');
        res.setMiercoles(dias.charAt(2) == '1');
        res.setJueves(dias.charAt(3) == '1');
        res.setViernes(dias.charAt(4) == '1');
        res.setSabado(dias.charAt(5) == '1');
        res.setDomingo(dias.charAt(6) == '1');

        res.setHorarioAtencionInicio(d.getHorarioAtencionInicio());
        res.setHorarioAtencionFin(d.getHorarioAtencionFin());
        res.setDuracionVisita(d.getDuracionVisita());

        return res;
    }
}
