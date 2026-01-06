/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.RealizarPagoRequest;
import com.inmapi.dto.RealizarPagoResponse;
import com.inmapi.dto.TipoPagoResponse;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class PagoServiceTest {

    @Mock private TipoPagoRepository tiposPago;
    @Mock private AccesoVendedorRepository accesos;
    @Mock private ClienteRepository clientes;
    @Mock private PublicacionRepository publicaciones;

    @InjectMocks
    private PagoService service;

    // --- Helpers de Seguridad ---
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

    // --- Pruebas de Listado ---

    @Test
    void obtenerTiposPago_Exito() {
        TipoPago t1 = new TipoPago(); t1.setId(1); t1.setTipoPago("Tarjeta");
        TipoPago t2 = new TipoPago(); t2.setId(2); t2.setTipoPago("PayPal");

        when(tiposPago.findAll()).thenReturn(List.of(t1, t2));

        List<TipoPagoResponse> res = service.obtenerTiposPago();

        assertEquals(2, res.size());
        assertEquals("Tarjeta", res.get(0).getNombre());
    }

    // --- Pruebas de Pagos ---

    @Test
    void pagarAcceso_NoEsCliente_DebeLanzar403() {
        simularVendedor();
        
        assertThrows(ResponseStatusException.class, 
            () -> service.pagarAcceso(1, new RealizarPagoRequest()));
    }

    @Test
    void pagarAcceso_MontoInvalido_DebeLanzar422() {
        simularCliente("cliente@test.com");
        RealizarPagoRequest req = new RealizarPagoRequest();
        req.setMonto(0.0); // Inválido

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.pagarAcceso(1, req));
        
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void pagarAcceso_YaTieneAcceso_DebeRetornarExitoSinCobrar() {
        // 1. GIVEN
        String email = "cliente@test.com";
        simularCliente(email);
        Integer idPub = 10;

        Cliente c = new Cliente(); c.setId(1); c.setCorreo(email);
        Publicacion p = new Publicacion(); p.setId(idPub);
        
        // Simulamos que YA existe un acceso en la base de datos
        AccesoVendedor accesoPrevio = new AccesoVendedor();
        accesoPrevio.setId(500);
        accesoPrevio.setMonto(50.0);

        when(clientes.findByCorreo(email)).thenReturn(Optional.of(c));
        when(publicaciones.findById(idPub)).thenReturn(Optional.of(p));
        when(accesos.findByClienteIdAndPublicacionId(1, 10)).thenReturn(Optional.of(accesoPrevio));

        RealizarPagoRequest req = new RealizarPagoRequest();
        req.setMonto(50.0);

        // 2. WHEN
        RealizarPagoResponse res = service.pagarAcceso(idPub, req);

        // 3. THEN
        assertTrue(res.isExito());
        assertTrue(res.isYaTeniaAcceso(), "Debe indicar que ya tenía acceso");
        assertEquals(500, res.getIdAcceso());
        
        // Verificar que NO se guardó nada nuevo
        verify(accesos, never()).save(any());
    }

    @Test
    void pagarAcceso_PagoRechazado_SimulacionFalla() {
        // 1. GIVEN
        String email = "c@test.com";
        simularCliente(email);
        
        Cliente c = new Cliente(); c.setId(1); c.setCorreo(email);
        Publicacion p = new Publicacion(); p.setId(10);
        TipoPago tp = new TipoPago(); tp.setId(1);

        when(clientes.findByCorreo(email)).thenReturn(Optional.of(c));
        when(publicaciones.findById(10)).thenReturn(Optional.of(p));
        when(accesos.findByClienteIdAndPublicacionId(1, 10)).thenReturn(Optional.empty());
        when(tiposPago.findById(1)).thenReturn(Optional.of(tp));

        RealizarPagoRequest req = new RealizarPagoRequest();
        req.setIdTipoPago(1);
        req.setMonto(100.0);
        req.setDatosSimulados(""); // VACÍO -> Debe fallar según tu lógica

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.pagarAcceso(10, req));
        
        assertEquals(HttpStatus.PAYMENT_REQUIRED, ex.getStatusCode()); // 402 Payment Required
    }

    @Test
    void pagarAcceso_Exito_DebeGuardarAcceso() {
        // 1. GIVEN
        String email = "rico@test.com";
        simularCliente(email);
        Integer idPub = 20;

        Cliente c = new Cliente(); c.setId(2); c.setCorreo(email);
        Publicacion p = new Publicacion(); p.setId(idPub);
        TipoPago tp = new TipoPago(); tp.setId(5); tp.setTipoPago("Bitcoin");

        when(clientes.findByCorreo(email)).thenReturn(Optional.of(c));
        when(publicaciones.findById(idPub)).thenReturn(Optional.of(p));
        when(accesos.findByClienteIdAndPublicacionId(2, 20)).thenReturn(Optional.empty()); // No tiene acceso aún
        when(tiposPago.findById(5)).thenReturn(Optional.of(tp));
        
        // Mock del guardado para devolver ID
        when(accesos.save(any(AccesoVendedor.class))).thenAnswer(inv -> {
            AccesoVendedor av = inv.getArgument(0);
            av.setId(999);
            return av;
        });

        RealizarPagoRequest req = new RealizarPagoRequest();
        req.setIdTipoPago(5);
        req.setMonto(200.0);
        req.setDatosSimulados("Wallet123"); // Datos válidos

        // 2. WHEN
        RealizarPagoResponse res = service.pagarAcceso(idPub, req);

        // 3. THEN
        assertTrue(res.isExito());
        assertFalse(res.isYaTeniaAcceso());
        assertEquals(999, res.getIdAcceso());
        
        // --- CAMBIO: Usar getTipoPago() en lugar de getNombreTipoPago() ---
        assertEquals("Bitcoin", res.getTipoPago());
        
        verify(accesos).save(any(AccesoVendedor.class));
    }
}