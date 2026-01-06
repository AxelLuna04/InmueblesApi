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

import java.time.LocalTime;
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
public class AgendaServiceTest {

    @Mock private VendedorRepository vendedores;
    @Mock private DisponibilidadRepository disponibilidades;

    @InjectMocks
    private AgendaService service;

    // --- Helpers de Seguridad ---
    private void simularVendedor(String email) {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_VENDEDOR"));
        var auth = new UsernamePasswordAuthenticationToken(email, null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void simularCliente() {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        var auth = new UsernamePasswordAuthenticationToken("cliente@test.com", null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obtenerAgenda_NoEsVendedor_DebeLanzar403() {
        // 1. GIVEN
        simularCliente();

        // 2. WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> service.obtenerAgenda());
    }

    @Test
    void obtenerAgenda_NoExisteConfiguracion_DebeLanzar404() {
        // 1. GIVEN
        String email = "vendedor@test.com";
        simularVendedor(email);

        Vendedor v = new Vendedor();
        v.setId(1);
        v.setCorreo(email);

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(v));
        when(disponibilidades.findByVendedorId(1)).thenReturn(Optional.empty());

        // 2. WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> service.obtenerAgenda());
    }

    @Test
    void guardarAgenda_ValidacionesLogicas_DebeLanzar422() {
        // 1. GIVEN
        simularVendedor("v@test.com");
        
        // Caso A: Sin días seleccionados
        ConfigurarAgendaRequest reqSinDias = new ConfigurarAgendaRequest();
        reqSinDias.setHorarioAtencionInicio(LocalTime.of(9, 0));
        reqSinDias.setHorarioAtencionFin(LocalTime.of(18, 0));
        reqSinDias.setDuracionVisita(30.0); // CORREGIDO: .0
        // Todos los días false por defecto
        
        assertThrows(ResponseStatusException.class, () -> service.guardarAgenda(reqSinDias));

        // Caso B: Hora Fin antes que Inicio
        ConfigurarAgendaRequest reqHorarioMal = new ConfigurarAgendaRequest();
        reqHorarioMal.setLunes(true);
        reqHorarioMal.setHorarioAtencionInicio(LocalTime.of(18, 0));
        reqHorarioMal.setHorarioAtencionFin(LocalTime.of(9, 0)); // ERROR
        reqHorarioMal.setDuracionVisita(30.0); // CORREGIDO: .0

        assertThrows(ResponseStatusException.class, () -> service.guardarAgenda(reqHorarioMal));

        // Caso C: Duración 0 o negativa
        ConfigurarAgendaRequest reqDuracionMal = new ConfigurarAgendaRequest();
        reqDuracionMal.setLunes(true);
        reqDuracionMal.setHorarioAtencionInicio(LocalTime.of(9, 0));
        reqDuracionMal.setHorarioAtencionFin(LocalTime.of(10, 0));
        reqDuracionMal.setDuracionVisita(0.0); // ERROR (y CORREGIDO .0)

        assertThrows(ResponseStatusException.class, () -> service.guardarAgenda(reqDuracionMal));
    }

    @Test
    void guardarAgenda_Exito_DebeGuardarYConvertirDias() {
        // 1. GIVEN
        String email = "vendedor@test.com";
        simularVendedor(email);

        Vendedor v = new Vendedor();
        v.setId(5);
        v.setCorreo(email);

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(v));
        // Simulamos que no tiene agenda previa, crea una nueva
        when(disponibilidades.findByVendedorId(5)).thenReturn(Optional.empty());
        
        // Mock del save para que devuelva lo mismo que le entra
        when(disponibilidades.save(any(Disponibilidad.class))).thenAnswer(i -> i.getArgument(0));

        ConfigurarAgendaRequest req = new ConfigurarAgendaRequest();
        req.setLunes(true);
        req.setMiercoles(true); // Lunes y Miércoles = "1010000"
        req.setHorarioAtencionInicio(LocalTime.of(9, 0));
        req.setHorarioAtencionFin(LocalTime.of(14, 0));
        req.setDuracionVisita(60.0); // CORREGIDO: .0

        // 2. WHEN
        ConfigurarAgendaResponse res = service.guardarAgenda(req);

        // 3. THEN
        assertTrue(res.getLunes());
        assertFalse(res.getMartes());
        assertTrue(res.getMiercoles());
        
        // Verificamos que al repositorio llegó la cadena correcta
        verify(disponibilidades).save(argThat(d -> 
            d.getDiasDisponibles().equals("1010000") && 
            d.getDiasNoDisponibles().equals("0101111") // El inverso
        ));
    }

    @Test
    void guardarAgendaPorId_Exito_AdminPuedeConfigurar() {
        // Este método no valida rol en el código (se asume que el controller o quien lo llame valida)
        // Probamos la lógica pura
        
        Integer idVendedor = 10;
        Vendedor v = new Vendedor();
        v.setId(idVendedor);

        when(vendedores.findById(idVendedor)).thenReturn(Optional.of(v));
        when(disponibilidades.findByVendedorId(idVendedor)).thenReturn(Optional.empty());
        when(disponibilidades.save(any())).thenAnswer(i -> i.getArgument(0));

        ConfigurarAgendaRequest req = new ConfigurarAgendaRequest();
        req.setDomingo(true); // "0000001"
        req.setHorarioAtencionInicio(LocalTime.of(10, 0));
        req.setHorarioAtencionFin(LocalTime.of(11, 0));
        req.setDuracionVisita(15.0); // CORREGIDO: .0

        // WHEN
        ConfigurarAgendaResponse res = service.guardarAgendaPorId(idVendedor, req);

        // THEN
        assertTrue(res.getDomingo());
        assertFalse(res.getLunes());
        
        verify(disponibilidades).save(argThat(d -> 
            d.getDiasDisponibles().equals("0000001")
        ));
    }
}