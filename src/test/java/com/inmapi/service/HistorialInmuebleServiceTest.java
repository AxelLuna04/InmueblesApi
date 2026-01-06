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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HistorialInmuebleServiceTest {

    @Mock private MovimientoRepository movimientos;
    @Mock private PublicacionRepository publicaciones;

    @InjectMocks
    private HistorialInmuebleService service;

    // --- Helpers de Seguridad ---
    private void simularAdmin() {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var auth = new UsernamePasswordAuthenticationToken("admin@test.com", null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void simularUsuarioNormal() {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        var auth = new UsernamePasswordAuthenticationToken("user@test.com", null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obtenerHistorial_NoEsAdmin_DebeLanzar403() {
        // 1. GIVEN
        simularUsuarioNormal();

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.obtenerHistorial(1, null, null, null));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Solo administradores pueden ver el historial", ex.getReason());
    }

    @Test
    void obtenerHistorial_PublicacionNoExiste_DebeLanzar404() {
        // 1. GIVEN
        simularAdmin();
        when(publicaciones.findById(99)).thenReturn(Optional.empty());

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.obtenerHistorial(99, null, null, null));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void obtenerHistorial_Exito_SinFiltros() {
        // 1. GIVEN
        simularAdmin();
        Publicacion p = new Publicacion();
        p.setId(10);
        p.setPrecio(1500000.0);

        // Movimiento 1: Creación
        Movimiento m1 = new Movimiento();
        m1.setId(1);
        m1.setTipoMovimiento("CREACION");
        m1.setFecha(LocalDate.of(2025, 1, 10));
        m1.setPublicacion(p);

        // Movimiento 2: Venta (Con cliente)
        Cliente c = new Cliente();
        c.setNombreCompleto("Comprador Feliz");
        Movimiento m2 = new Movimiento();
        m2.setId(2);
        m2.setTipoMovimiento("VENTA");
        m2.setFecha(LocalDate.of(2025, 2, 20));
        m2.setArrendador(c);
        m2.setPublicacion(p);

        when(publicaciones.findById(10)).thenReturn(Optional.of(p));
        // Simulamos que la BD devuelve la lista
        when(movimientos.findByPublicacionIdOrderByFechaDesc(10)).thenReturn(List.of(m1, m2));

        // 2. WHEN
        List<MovimientoHistorialResponse> res = service.obtenerHistorial(10, null, null, null);

        // 3. THEN
        assertNotNull(res);
        assertEquals(2, res.size());
        
        // Verificar mapeo del Movimiento 1 (Creación)
        MovimientoHistorialResponse r1 = res.get(0);
        assertEquals("CREACION", r1.getTipoMovimiento());
        assertEquals("Publicación creada", r1.getDescripcion());
        assertNull(r1.getPrecio()); // Creación no suele llevar precio en tu lógica DTO

        // Verificar mapeo del Movimiento 2 (Venta)
        MovimientoHistorialResponse r2 = res.get(1);
        assertEquals("VENTA", r2.getTipoMovimiento());
        assertEquals("Comprador Feliz", r2.getNombreCliente());
        assertEquals(1500000.0, r2.getPrecio()); // Venta sí lleva precio
        assertTrue(r2.getDescripcion().contains("Inmueble vendido"));
    }

    @Test
    void obtenerHistorial_ConFiltros_DebeFiltrarResultados() {
        // 1. GIVEN
        simularAdmin();
        Publicacion p = new Publicacion();
        p.setId(10);

        Movimiento antiguo = new Movimiento();
        antiguo.setTipoMovimiento("EDICION");
        antiguo.setFecha(LocalDate.of(2024, 1, 1)); // Fecha vieja

        Movimiento reciente = new Movimiento();
        reciente.setTipoMovimiento("APROBACION");
        reciente.setFecha(LocalDate.of(2025, 5, 5)); // Fecha objetivo

        when(publicaciones.findById(10)).thenReturn(Optional.of(p));
        // La BD devuelve todo, el servicio filtra en memoria
        when(movimientos.findByPublicacionIdOrderByFechaDesc(10)).thenReturn(List.of(antiguo, reciente));

        // Filtramos por fecha (solo el de 2025)
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);

        // 2. WHEN
        List<MovimientoHistorialResponse> res = service.obtenerHistorial(10, inicio, fin, "APROBACION");

        // 3. THEN
        assertEquals(1, res.size());
        assertEquals("APROBACION", res.get(0).getTipoMovimiento());
        // El "antiguo" fue descartado por fecha y tipo
    }
}