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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

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
public class PublicacionQueryServiceTest {

    @Mock private PublicacionRepository publicaciones;
    @Mock private FotoPublicacionRepository fotos;
    @Mock private ClienteRepository clientes;
    @Mock private MediaUrlBuilder urlBuilder;

    @InjectMocks
    private PublicacionQueryService service;

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

    // --- Pruebas Recomendación (paraTi) ---

    @Test
    void paraTi_Exito_OrdenaPorPuntaje() {
        // 1. GIVEN
        String email = "cliente@test.com";
        simularCliente(email);

        // Cliente con preferencias
        Cliente c = new Cliente();
        c.setCorreo(email);
        c.setPresupuesto(10000.0);
        c.setUbicacionInteres("Centro");
        c.setNumeroMiembrosFamilia("1"); 

        when(clientes.findByCorreo(email)).thenReturn(Optional.of(c));

        // Publicación 1: Perfecta (En precio y ubicación) -> Score alto
        Publicacion p1 = new Publicacion();
        p1.setId(1);
        p1.setPrecio(9000.0); // Bien
        p1.setDireccion(crearDireccion("Calle 1, Centro")); // Bien
        p1.setTitulo("Casa Perfecta");

        // Publicación 2: Mala (Muy cara y lejos) -> Score bajo o negativo
        Publicacion p2 = new Publicacion();
        p2.setId(2);
        p2.setPrecio(50000.0); // Mal (muy caro)
        p2.setDireccion(crearDireccion("Calle Lejana, Norte")); // Mal
        p2.setTitulo("Casa Cara");

        // Publicación 3: Regular (Precio bien, ubicación mal) -> Score medio
        Publicacion p3 = new Publicacion();
        p3.setId(3);
        p3.setPrecio(8000.0); // Bien
        p3.setDireccion(crearDireccion("Calle X, Sur")); // Mal
        p3.setTitulo("Casa Regular");

        // Simulamos que el repositorio devuelve todas
        when(publicaciones.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(p1, p2, p3));

        // 2. WHEN
        Page<PublicacionCard> result = service.paraTi(0, 10);

        // 3. THEN
        List<PublicacionCard> lista = result.getContent();
        
        // Verificar orden: P1 (90pts) > P3 (40pts) > P2 (descartada o ultima si >0)
        // Nota: P2 tiene precio muy alto (-10) y ubicación mal (0) -> Score -10. Se filtra (>0).
        assertEquals(2, lista.size()); // Solo p1 y p3 deberían pasar el filtro score > 0
        assertEquals("Casa Perfecta", lista.get(0).getTitulo());
        assertEquals("Casa Regular", lista.get(1).getTitulo());
    }

    @Test
    void paraTi_Estudiante_PriorizaRentasBaratas() {
        // 1. GIVEN
        String email = "estudiante@test.com";
        simularCliente(email);

        Ocupacion oc = new Ocupacion();
        oc.setNombre("Estudiante Universitario");
        
        Cliente c = new Cliente();
        c.setOcupacion(oc);
        when(clientes.findByCorreo(email)).thenReturn(Optional.of(c));

        Publicacion pRentaBarata = new Publicacion();
        pRentaBarata.setTitulo("Cuarto Barato");
        pRentaBarata.setTipoOperacion("RENTA");
        pRentaBarata.setPrecio(3000.0); // < 5000

        Publicacion pVentaCara = new Publicacion();
        pVentaCara.setTitulo("Casa Venta");
        pVentaCara.setTipoOperacion("VENTA");
        pVentaCara.setPrecio(1000000.0);

        when(publicaciones.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(pRentaBarata, pVentaCara));

        // 2. WHEN
        Page<PublicacionCard> res = service.paraTi(0, 10);

        // 3. THEN
        // El estudiante recibe bono por RENTA y por PRECIO < 5000
        // La venta cara no suma nada y se filtra (score 0)
        assertEquals(1, res.getContent().size());
        assertEquals("Cuarto Barato", res.getContent().get(0).getTitulo());
    }

    // --- Pruebas Listar (Filtros) ---

    @Test
    void listar_Exito_LlamaRepositorio() {
        // 1. GIVEN
        PublicacionFiltro filtro = new PublicacionFiltro();
        filtro.setPrecioMin(1000.0);
        filtro.setCaracteristicas("1,2,3"); // CSV

        Publicacion p = new Publicacion();
        p.setTitulo("Resultado");
        Page<Publicacion> pagina = new PageImpl<>(List.of(p));

        when(publicaciones.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(pagina);

        // 2. WHEN
        Page<PublicacionCard> res = service.listar(filtro, 0, 10, "precio-asc");

        // 3. THEN
        assertEquals(1, res.getTotalElements());
        assertEquals("Resultado", res.getContent().get(0).getTitulo());
        
        // Verificamos que se parseó el CSV de características
        // (Es difícil verificar el contenido exacto de la Specification con mocks,
        // pero verificamos que no falló el parseo).
    }

    // --- Pruebas Detalle ---

    @Test
    void detalle_Aprobada_RetornaInfo() {
        // 1. GIVEN
        Integer id = 5;
        Publicacion p = new Publicacion();
        p.setId(id);
        p.setEstado("APROBADA"); // Importante
        p.setTitulo("Detalle");
        p.setFotos(new ArrayList<>());
        p.setCaracteristicas(new ArrayList<>());

        when(publicaciones.findById(id)).thenReturn(Optional.of(p));

        // 2. WHEN
        PublicacionDetalle det = service.detalle(id);

        // 3. THEN
        assertNotNull(det);
        assertEquals("Detalle", det.getTitulo());
    }

    @Test
    void detalle_NoAprobada_Lanza403() {
        // 1. GIVEN
        Integer id = 5;
        Publicacion p = new Publicacion();
        p.setEstado("PENDIENTE"); // No aprobada

        when(publicaciones.findById(id)).thenReturn(Optional.of(p));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.detalle(id));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // Helper
    private Direccion crearDireccion(String texto) {
        Direccion d = new Direccion();
        d.setFormattedAddress(texto);
        return d;
    }
}