package com.inmapi.service;
import com.inmapi.dto.*;
import com.inmapi.modelo.*;
import com.inmapi.repository.FotoPublicacionRepository;
import com.inmapi.repository.MovimientoRepository;
import com.inmapi.repository.PublicacionRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminPublicacionServiceTest {

    @Mock private PublicacionRepository publicaciones;
    @Mock private FotoPublicacionRepository fotos;
    @Mock private EmailService email;
    @Mock private EmailTemplates templates;
    @Mock private MediaUrlBuilder urlBuilder;
    @Mock private MovimientoRepository movimientoRepository;

    @InjectMocks
    private AdminPublicacionService adminService;

    // Métodos de ayuda para simular la seguridad
    private void simularUsuarioAdmin() {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var auth = new UsernamePasswordAuthenticationToken("admin@test.com", null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void simularUsuarioNoAdmin() {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var auth = new UsernamePasswordAuthenticationToken("user@test.com", null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void aprobar_Exitoso_DebeCambiarEstadoYRegistrarMovimiento() {
        // 1. GIVEN (Preparación)
        simularUsuarioAdmin();
        Integer pubId = 1;
        
        Publicacion p = new Publicacion();
        p.setId(pubId);
        p.setEstado("PENDIENTE");
        p.setTitulo("Casa de Lujo");
        
        Vendedor v = new Vendedor();
        v.setCorreo("vendedor@test.com");
        v.setNombreCompleto("Juan Perez");
        p.setVendedor(v);

        when(publicaciones.findById(pubId)).thenReturn(Optional.of(p));
        
        // 2. WHEN (Acción)
        ModeracionResponse response = adminService.aprobar(pubId);

        // 3. THEN (Verificación)
        assertEquals("APROBADA", p.getEstado());
        assertEquals("APROBADA", response.getEstado());
        
        // Verificamos que se guardó la publicación y el movimiento
        verify(publicaciones).save(p);
        verify(movimientoRepository).save(any(Movimiento.class));
        
        // Verificamos que se intentó enviar el email
        verify(email).enviar(eq("vendedor@test.com"), anyString(), any(), any());
    }

    @Test
    void aprobar_SinSerAdmin_DebeLanzarExcepcion403() {
        // 1. GIVEN
        simularUsuarioNoAdmin();
        
        // 2. WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> adminService.aprobar(1));
        // Verificamos que nunca se llamó al repositorio porque el filtro de seguridad actuó antes
        verify(publicaciones, never()).save(any());
    }

    @Test
    void rechazar_SinMotivo_DebeLanzarError422() {
        // 1. GIVEN
        simularUsuarioAdmin();
        Integer pubId = 1;
        Publicacion p = new Publicacion();
        p.setEstado("PENDIENTE");
        
        when(publicaciones.findById(pubId)).thenReturn(Optional.of(p));

        // 2. WHEN & THEN
        // Motivo null o vacío debe fallar según tu lógica de AdminPublicacionService
        assertThrows(ResponseStatusException.class, () -> adminService.rechazar(pubId, ""));
    }

    @Test
    void detalle_PublicacionNoExiste_DebeLanzar404() {
        // 1. GIVEN
        simularUsuarioAdmin();
        when(publicaciones.findById(99)).thenReturn(Optional.empty());

        // 2. WHEN & THEN
        assertThrows(ResponseStatusException.class, () -> adminService.detalle(99));
    }
}