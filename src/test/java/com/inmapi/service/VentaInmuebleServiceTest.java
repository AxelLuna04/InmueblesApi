/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.InteresadoResponse;
import com.inmapi.dto.VenderInmuebleRequest;
import com.inmapi.dto.VenderInmuebleResponse;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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
public class VentaInmuebleServiceTest {

    @Mock private PublicacionRepository publicaciones;
    @Mock private VendedorRepository vendedores;
    @Mock private ClienteRepository clientes;
    @Mock private AccesoVendedorRepository accesos;
    @Mock private MovimientoRepository movimientos;
    @Mock private ContratoRepository contratos;

    @InjectMocks
    private VentaInmuebleService service;

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

    // --- Pruebas Listar Interesados ---

    @Test
    void listarInteresados_Exito_DebeRetornarUnicos() {
        // 1. GIVEN
        String email = "yo@test.com";
        simularVendedor(email);

        Vendedor v = new Vendedor();
        v.setId(1);
        v.setCorreo(email);

        Publicacion p = new Publicacion();
        p.setId(10);
        p.setVendedor(v);

        // Cliente que pagó 2 veces (simulado)
        Cliente c1 = new Cliente();
        c1.setId(5);
        c1.setNombreCompleto("Juan");
        
        AccesoVendedor a1 = new AccesoVendedor(); a1.setCliente(c1);
        AccesoVendedor a2 = new AccesoVendedor(); a2.setCliente(c1); // Duplicado

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(v));
        when(publicaciones.findById(10)).thenReturn(Optional.of(p));
        when(accesos.findByPublicacionId(10)).thenReturn(List.of(a1, a2));

        // 2. WHEN
        List<InteresadoResponse> res = service.listarInteresados(10);

        // 3. THEN
        assertEquals(1, res.size()); // Solo debe salir Juan una vez
        
        // --- CORRECCIÓN AQUÍ: getNombreCompleto() en vez de getNombre() ---
        assertEquals("Juan", res.get(0).getNombreCompleto());
    }

    @Test
    void listarInteresados_NoEsDuenio_DebeLanzar403() {
        // 1. GIVEN
        String email = "hacker@test.com";
        simularVendedor(email);

        Vendedor yo = new Vendedor();
        yo.setId(99);
        yo.setCorreo(email);

        Vendedor otro = new Vendedor();
        otro.setId(1);

        Publicacion pAjena = new Publicacion();
        pAjena.setId(10);
        pAjena.setVendedor(otro);

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(yo));
        when(publicaciones.findById(10)).thenReturn(Optional.of(pAjena));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.listarInteresados(10));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("No puedes vender una publicación que no es tuya", ex.getReason());
    }

    // --- Pruebas Vender Inmueble ---

    @Test
    void venderInmueble_CompradorSinAcceso_DebeLanzar403() {
        // 1. GIVEN
        String email = "v@test.com";
        simularVendedor(email);

        Vendedor v = new Vendedor();
        v.setId(1);
        v.setCorreo(email);

        Publicacion p = new Publicacion();
        p.setId(10);
        p.setVendedor(v);
        p.setEstado("APROBADA");

        Cliente comprador = new Cliente();
        comprador.setId(5);

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(v));
        when(publicaciones.findById(10)).thenReturn(Optional.of(p));
        when(accesos.existsByPublicacionId(10)).thenReturn(true); // Hay interesados en general
        when(clientes.findById(5)).thenReturn(Optional.of(comprador));
        
        // El cliente 5 NO tiene acceso a la publicación 10
        when(accesos.findByClienteIdAndPublicacionId(5, 10)).thenReturn(Optional.empty());

        VenderInmuebleRequest req = new VenderInmuebleRequest();
        req.setIdClienteComprador(5);

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.venderInmueble(10, req, null));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("no ha pagado acceso"));
    }

    @Test
    void venderInmueble_Exito_ConContrato() {
        // 1. GIVEN
        String email = "v@test.com";
        simularVendedor(email);

        Vendedor v = new Vendedor(); v.setId(1); v.setCorreo(email);
        Publicacion p = new Publicacion(); p.setId(10); p.setVendedor(v); p.setEstado("APROBADA");
        Cliente c = new Cliente(); c.setId(5);
        
        // Simular que el cliente sí pagó acceso
        AccesoVendedor acceso = new AccesoVendedor();
        acceso.setTipoPago(new TipoPago());
        
        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(v));
        when(publicaciones.findById(10)).thenReturn(Optional.of(p));
        when(accesos.existsByPublicacionId(10)).thenReturn(true);
        when(clientes.findById(5)).thenReturn(Optional.of(c));
        when(accesos.findByClienteIdAndPublicacionId(5, 10)).thenReturn(Optional.of(acceso));

        // Mock de guardado
        when(movimientos.save(any(Movimiento.class))).thenAnswer(i -> {
            Movimiento m = i.getArgument(0);
            m.setId(100);
            return m;
        });
        when(contratos.save(any(Contrato.class))).thenAnswer(i -> {
            Contrato ct = i.getArgument(0);
            ct.setId(200);
            return ct;
        });

        VenderInmuebleRequest req = new VenderInmuebleRequest();
        req.setIdClienteComprador(5);
        req.setFechaVenta(LocalDate.now());

        MultipartFile pdf = mock(MultipartFile.class);
        when(pdf.isEmpty()).thenReturn(false);
        when(pdf.getOriginalFilename()).thenReturn("contrato.pdf");

        // 2. WHEN
        VenderInmuebleResponse res = service.venderInmueble(10, req, pdf);

        // 3. THEN
        assertEquals("VENDIDA", p.getEstado()); // Estado cambió
        assertEquals(100, res.getIdMovimiento());
        assertEquals(200, res.getIdContrato());
        
        verify(publicaciones).save(p);
        verify(movimientos).save(any(Movimiento.class));
        verify(contratos).save(any(Contrato.class));
    }
}