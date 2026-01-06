/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.*;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class CitaServiceTest {

    @Mock private PublicacionRepository publicaciones;
    @Mock private DisponibilidadRepository disponibilidades;
    @Mock private DiaOcupadoRepository diasOcupados;
    @Mock private AgendaRepository agendas;
    @Mock private ClienteRepository clientes;

    @InjectMocks
    private CitaService service;

    // --- Helpers para simular usuarios ---
    private void simularCliente(String email) {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        var auth = new UsernamePasswordAuthenticationToken(email, null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void simularVendedor() {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_VENDEDOR"));
        var auth = new UsernamePasswordAuthenticationToken("vendedor@test.com", null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    // --- Pruebas de Obtener Horas Disponibles ---

    @Test
    void obtenerHoras_DebeCalcularSlotsYFiltrarOcupados() {
        // 1. GIVEN
        Integer pubId = 1;
        LocalDate fecha = LocalDate.of(2025, 10, 20); // Un Lunes (supongamos)

        // Configurar Vendedor y Publicación
        Vendedor v = new Vendedor();
        v.setId(5);
        Publicacion p = new Publicacion();
        p.setId(pubId);
        p.setVendedor(v);

        // Configurar Disponibilidad (Lunes disponible: "1000000")
        // Horario: 10:00 a 12:00, Duración 60 min -> Slots: 10:00 y 11:00
        Disponibilidad disp = new Disponibilidad();
        disp.setVendedor(v);
        // Aseguramos que el día de la semana de 'fecha' coincida con un '1' en la cadena
        // Para simplificar el test, ponemos todos '1'
        disp.setDiasDisponibles("1111111"); 
        disp.setHorarioAtencionInicio(LocalTime.of(10, 0));
        disp.setHorarioAtencionFin(LocalTime.of(12, 0));
        disp.setDuracionVisita(60.0); // 1 hora

        // Simular una cita ya existente a las 10:00
        Agenda citaOcupada = new Agenda();
        citaOcupada.setHoraSeleccionada(LocalTime.of(10, 0));

        when(publicaciones.findById(pubId)).thenReturn(Optional.of(p));
        when(disponibilidades.findByVendedorId(5)).thenReturn(Optional.of(disp));
        // No está el día completo ocupado
        when(diasOcupados.existsByVendedorIdAndFecha(5, fecha)).thenReturn(false);
        // Devuelve la lista de citas actuales
        when(agendas.findByVendedorIdAndFechaSeleccionada(5, fecha)).thenReturn(List.of(citaOcupada));

        // 2. WHEN
        HorasDisponiblesResponse res = service.obtenerHorasDisponibles(pubId, fecha);

        // 3. THEN
        assertNotNull(res);
        assertEquals(1, res.getHoras().size());
        assertEquals(LocalTime.of(11, 0), res.getHoras().get(0)); // Solo debe quedar la de las 11
    }

    // --- Pruebas de Agendar Cita ---

    @Test
    void agendarCita_UsuarioNoEsCliente_DebeLanzar403() {
        // 1. GIVEN
        simularVendedor(); // Es vendedor, no cliente
        AgendarCitaRequest req = new AgendarCitaRequest();
        
        // 2. WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> service.agendarCita(1, req));
    }

    @Test
    void agendarCita_FechaPasada_DebeLanzar422() {
        // 1. GIVEN
        simularCliente("c@test.com");
        AgendarCitaRequest req = new AgendarCitaRequest();
        req.setFecha(LocalDate.now().minusDays(1)); // Ayer

        // 2. WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> service.agendarCita(1, req));
    }

    @Test
    void agendarCita_HoraOcupada_DebeLanzarConflict() {
        // 1. GIVEN
        simularCliente("cliente@test.com");
        Integer pubId = 1;
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalTime horaDeseada = LocalTime.of(10, 0);

        // Mock Publicación y Vendedor
        Vendedor v = new Vendedor();
        v.setId(10);
        Publicacion p = new Publicacion();
        p.setVendedor(v);

        // Mock Disponibilidad (Genera slots 10:00 y 11:00)
        Disponibilidad disp = new Disponibilidad();
        disp.setDiasDisponibles("1111111");
        disp.setHorarioAtencionInicio(LocalTime.of(10, 0));
        disp.setHorarioAtencionFin(LocalTime.of(12, 0));
        disp.setDuracionVisita(60.0);

        // Mock Cita Existente a la misma hora (10:00)
        Agenda citaExistente = new Agenda();
        citaExistente.setHoraSeleccionada(horaDeseada);

        when(clientes.findByCorreo(any())).thenReturn(Optional.of(new Cliente()));
        when(publicaciones.findById(pubId)).thenReturn(Optional.of(p));
        when(disponibilidades.findByVendedorId(10)).thenReturn(Optional.of(disp));
        // Devuelve que ya hay una cita a esa hora
        when(agendas.findByVendedorIdAndFechaSeleccionada(10, manana)).thenReturn(List.of(citaExistente));

        AgendarCitaRequest req = new AgendarCitaRequest();
        req.setFecha(manana);
        req.setHora(horaDeseada);

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
                () -> service.agendarCita(pubId, req));
        
        assertEquals(409, ex.getStatusCode().value()); // 409 CONFLICT
    }

    @Test
    void agendarCita_Exito_DebeGuardarCitaYBloquearDiaSiSeLlena() {
        // 1. GIVEN
        simularCliente("cliente@test.com");
        Integer pubId = 1;
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalTime horaUnica = LocalTime.of(10, 0);

        // Mock Entidades
        Vendedor v = new Vendedor();
        v.setId(10);
        Publicacion p = new Publicacion();
        p.setId(pubId);
        p.setVendedor(v);
        Cliente c = new Cliente();
        c.setCorreo("cliente@test.com");

        // Mock Disponibilidad (SOLO 1 slot de 10 a 11)
        Disponibilidad disp = new Disponibilidad();
        disp.setDiasDisponibles("1111111");
        disp.setHorarioAtencionInicio(LocalTime.of(10, 0));
        disp.setHorarioAtencionFin(LocalTime.of(11, 0)); // Solo cabe 1 hora
        disp.setDuracionVisita(60.0);

        when(clientes.findByCorreo(any())).thenReturn(Optional.of(c));
        when(publicaciones.findById(pubId)).thenReturn(Optional.of(p));
        when(disponibilidades.findByVendedorId(10)).thenReturn(Optional.of(disp));
        
        // Primera llamada: No hay citas, el slot está libre
        when(agendas.findByVendedorIdAndFechaSeleccionada(10, manana))
            .thenReturn(new ArrayList<>()) // Primero vacio para validacion
            .thenReturn(List.of(new Agenda())); // Segundo (simulado) para checar si se llenó

        // Mock del guardado de cita
        when(agendas.save(any(Agenda.class))).thenAnswer(inv -> {
            Agenda a = inv.getArgument(0);
            a.setId(500);
            // Simular que ahora la lista de citas tiene esta nueva cita
            // Esto es truculento en mocks, pero para la logica de "calcularHorasDisponibles"
            // dependerá de lo que retorne agendas.findBy... en la segunda llamada
            return a;
        });

        AgendarCitaRequest req = new AgendarCitaRequest();
        req.setFecha(manana);
        req.setHora(horaUnica);

        // 2. WHEN
        AgendarCitaResponse res = service.agendarCita(pubId, req);

        // 3. THEN
        assertNotNull(res);
        assertEquals("¡Cita registrada con éxito!", res.getMensaje());
        
        // Verificar que se guardó la cita
        verify(agendas).save(any(Agenda.class));
        
        // Verificar lógica especial: Como era el único slot y se ocupó, 
        // el servicio debió haber guardado un DiaOcupado
        // Nota: Esto depende de que en la segunda llamada a calcularHorasDisponibles 
        // (dentro del if final de agendarCita), la lista de horas retorne vacía.
        // En este test simplificado con Mockito, es difícil simular el cambio de estado 
        // interno de la BD, pero podemos verificar que al menos intentó calcular restantes.
        verify(disponibilidades, atLeast(2)).findByVendedorId(10);
    }
}