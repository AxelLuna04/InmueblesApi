/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.*;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final PublicacionRepository publicaciones;
    private final DisponibilidadRepository disponibilidades;
    private final DiaOcupadoRepository diasOcupados;
    private final AgendaRepository agendas;
    private final ClienteRepository clientes;
    private final VendedorRepository vendedores;


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
        String correo = emailActual();
        return vendedores.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
    }

    private boolean esCliente() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_CLIENTE"));
    }

    private Cliente clienteActual() {
        String correo = emailActual();
        return clientes.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }


    private Publicacion obtenerPublicacion(Integer idPublicacion) {
        return publicaciones.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
    }

    private Disponibilidad obtenerDisponibilidad(Vendedor vendedor) {
        return disponibilidades.findByVendedorId(vendedor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El vendedor no ha configurado su agenda"));
    }

    private boolean diaEnDisponibilidad(Disponibilidad disp, LocalDate fecha) {
        String dias = disp.getDiasDisponibles();
        if (dias == null || dias.length() != 7) return false;
        int index = fecha.getDayOfWeek().getValue() - 1;
        return dias.charAt(index) == '1';
    }

    private List<LocalTime> generarSlots(Disponibilidad disp) {
        LocalTime inicio = disp.getHorarioAtencionInicio();
        LocalTime fin = disp.getHorarioAtencionFin();

        if (inicio == null || fin == null || !inicio.isBefore(fin)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Horario de atención inválido para el vendedor");
        }

        int duracion = disp.getDuracionVisita() != null ? disp.getDuracionVisita().intValue() : 0;
        if (duracion <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Duración de visita inválida para el vendedor");
        }

        List<LocalTime> slots = new ArrayList<>();
        LocalTime actual = inicio;

        while (!actual.plusMinutes(duracion).isAfter(fin)) {
            slots.add(actual);
            actual = actual.plusMinutes(duracion);
        }

        return slots;
    }

    private List<LocalTime> calcularHorasDisponibles(Vendedor vendedor, LocalDate fecha) {
        Disponibilidad disp = obtenerDisponibilidad(vendedor);

        if (!diaEnDisponibilidad(disp, fecha)) {
            return List.of(); 
        }

       
        if (diasOcupados.existsByVendedorIdAndFecha(vendedor.getId(), fecha)) {
            return List.of();
        }

        
        List<LocalTime> slots = generarSlots(disp);

        
        List<Agenda> citas = agendas.findByVendedorIdAndFechaSeleccionada(vendedor.getId(), fecha);
        Set<LocalTime> horasOcupadas = citas.stream()
                .map(Agenda::getHoraSeleccionada)
                .collect(Collectors.toSet());

        return slots.stream()
                .filter(h -> !horasOcupadas.contains(h))
                .collect(Collectors.toList());
    }

    

    public CalendarioAgendaResponse obtenerCalendario(Integer idPublicacion, int anio, int mes) {
        Publicacion pub = obtenerPublicacion(idPublicacion);
        Vendedor vendedor = pub.getVendedor();
        Disponibilidad disp = obtenerDisponibilidad(vendedor);

        
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDate ultimoDia = primerDia.withDayOfMonth(primerDia.lengthOfMonth());

        List<DiaCalendarioAgendaResponse> dias = new ArrayList<>();

        for (LocalDate fecha = primerDia; !fecha.isAfter(ultimoDia); fecha = fecha.plusDays(1)) {
            DiaCalendarioAgendaResponse diaRes = new DiaCalendarioAgendaResponse();
            diaRes.setFecha(fecha);

            boolean habilitado = diaEnDisponibilidad(disp, fecha);
            diaRes.setHabilitado(habilitado);

            boolean lleno = false;
            if (habilitado) {
                lleno = diasOcupados.existsByVendedorIdAndFecha(vendedor.getId(), fecha);
            }
            diaRes.setLleno(lleno);

            dias.add(diaRes);
        }

        CalendarioAgendaResponse res = new CalendarioAgendaResponse();
        res.setAnio(anio);
        res.setMes(mes);
        res.setDias(dias);
        return res;
    }

    public HorasDisponiblesResponse obtenerHorasDisponibles(Integer idPublicacion, LocalDate fecha) {
        Publicacion pub = obtenerPublicacion(idPublicacion);
        Vendedor vendedor = pub.getVendedor();

        List<LocalTime> horas = calcularHorasDisponibles(vendedor, fecha);

        
        HorasDisponiblesResponse res = new HorasDisponiblesResponse();
        res.setFecha(fecha);
        res.setHoras(horas);
        return res;
    }

    @Transactional
    public AgendarCitaResponse agendarCita(Integer idPublicacion, AgendarCitaRequest req) {
        if (!esCliente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo clientes pueden agendar citas");
        }

        if (req.getFecha().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se pueden agendar citas en fechas pasadas");
        }

        Cliente cliente = clienteActual();
        Publicacion pub = obtenerPublicacion(idPublicacion);
        Vendedor vendedor = pub.getVendedor();

        
        List<LocalTime> horasDisponibles = calcularHorasDisponibles(vendedor, req.getFecha());
        if (horasDisponibles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El día seleccionado no tiene horarios disponibles");
        }

        if (!horasDisponibles.contains(req.getHora())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La hora seleccionada ya no está disponible");
        }

        
        Agenda cita = new Agenda();
        cita.setVendedor(vendedor);
        cita.setArrendador(cliente);
        cita.setPublicacion(pub);
        cita.setFechaSeleccionada(req.getFecha());
        cita.setHoraSeleccionada(req.getHora());

        cita = agendas.save(cita);

        
        List<LocalTime> restantes = calcularHorasDisponibles(vendedor, req.getFecha());
        if (restantes.isEmpty() && !diasOcupados.existsByVendedorIdAndFecha(vendedor.getId(), req.getFecha())) {
            DiaOcupado d = new DiaOcupado();
            d.setVendedor(vendedor);
            d.setFecha(req.getFecha());
            diasOcupados.save(d);
        }

        return new AgendarCitaResponse(
                cita.getId(),
                cita.getFechaSeleccionada(),
                cita.getHoraSeleccionada(),
                "¡Cita registrada con éxito!"
        );
    }
    
    public List<AgendaVendedorResponse> listarCitasPendientes() {
        if (!esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo vendedores pueden ver su agenda");
        }

        Vendedor vendedor = vendedorActual();
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        
        List<Agenda> citas = agendas.findByVendedorIdAndFechaSeleccionadaGreaterThanEqualOrderByFechaSeleccionadaAscHoraSeleccionadaAsc(
                vendedor.getId(), 
                hoy
        );

        return citas.stream()
                
                .filter(cita -> {
                    if (cita.getFechaSeleccionada().isEqual(hoy)) {
                        return cita.getHoraSeleccionada().isAfter(ahora);
                    }
                    return true;
                })
                .map(cita -> new AgendaVendedorResponse(
                        cita.getId(),
                        cita.getFechaSeleccionada(),
                        cita.getHoraSeleccionada(),
                        cita.getPublicacion().getTitulo(),
                        cita.getArrendador().getNombreCompleto(),
                        cita.getArrendador().getTelefono()
                ))
                .collect(Collectors.toList());
    }
}
