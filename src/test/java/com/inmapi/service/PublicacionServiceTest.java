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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class PublicacionServiceTest {

    @Mock private VendedorRepository vendedores;
    @Mock private TipoInmuebleRepository tipos;
    @Mock private DireccionRepository direcciones;
    @Mock private PublicacionRepository publicaciones;
    @Mock private ListaCaracteristicasRepository listas;
    @Mock private CaracteristicaRepository caracteristicas;
    @Mock private CaracteristicaSeleccionadaRepository seleccionadas;
    @Mock private FotoPublicacionRepository fotosRepo;
    @Mock private FotoService fotoService;
    @Mock private MovimientoRepository movimientoRepository;

    @InjectMocks
    private PublicacionService service;

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

    // --- Pruebas para CREAR ---

    @Test
    void crear_Exitoso_DebeGuardarTodo() {
        // 1. GIVEN
        String email = "vendedor@test.com";
        simularVendedor(email);

        // Datos de entrada
        CrearPublicacionRequest req = new CrearPublicacionRequest();
        req.setTitulo("Casa Linda");
        req.setIdTipoInmueble(1);
        req.setIndicePortada(0);
        req.setDireccion(new DireccionDTO());
        req.setCaracteristicasIds(List.of(10, 20)); // IDs de características

        MultipartFile fotoMock = mock(MultipartFile.class);
        List<MultipartFile> fotos = List.of(fotoMock);

        // Mocks de Base de Datos
        Vendedor v = new Vendedor();
        v.setId(1);
        v.setCorreo(email);

        TipoInmueble tipo = new TipoInmueble();
        tipo.setId(1);

        // Configurar reglas de características permitidas
        Caracteristica c1 = new Caracteristica(); c1.setId(10);
        Caracteristica c2 = new Caracteristica(); c2.setId(20);
        ListaCaracteristicas lc1 = new ListaCaracteristicas(); lc1.setCaracteristica(c1);
        ListaCaracteristicas lc2 = new ListaCaracteristicas(); lc2.setCaracteristica(c2);
        
        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(v));
        when(tipos.findById(1)).thenReturn(Optional.of(tipo));
        // Ambas características (10 y 20) están permitidas para este tipo de inmueble
        when(listas.findByTipoInmuebleId(1)).thenReturn(List.of(lc1, lc2));
        when(caracteristicas.findById(10)).thenReturn(Optional.of(c1));
        when(caracteristicas.findById(20)).thenReturn(Optional.of(c2));

        // Mock del save para devolver objeto con ID
        when(publicaciones.save(any(Publicacion.class))).thenAnswer(i -> {
            Publicacion p = i.getArgument(0);
            p.setId(100);
            return p;
        });

        // 2. WHEN
        CrearPublicacionResponse res = service.crear(req, fotos);

        // 3. THEN
        assertNotNull(res);
        assertEquals("PENDIENTE", res.getEstado());
        
        // Verificaciones
        verify(direcciones).save(any(Direccion.class));
        verify(publicaciones).save(any(Publicacion.class));
        verify(seleccionadas, times(2)).save(any(CaracteristicaSeleccionada.class)); // 2 características
        verify(fotoService).guardarFotoPublicacion(any(), any(), eq(true)); // Se guardó portada
        verify(movimientoRepository).save(any(Movimiento.class)); // Se registró historial
    }

    @Test
    void crear_NoEsVendedor_DebeLanzar403() {
        simularCliente(); // Es cliente, no vendedor
        CrearPublicacionRequest req = new CrearPublicacionRequest();
        
        assertThrows(ResponseStatusException.class, () -> service.crear(req, List.of()));
    }

    @Test
    void crear_SinFotos_DebeLanzar422() {
        simularVendedor("v@test.com");
        CrearPublicacionRequest req = new CrearPublicacionRequest();
        
        assertThrows(ResponseStatusException.class, () -> service.crear(req, null));
        assertThrows(ResponseStatusException.class, () -> service.crear(req, new ArrayList<>()));
    }

    @Test
    void crear_CaracteristicaNoPermitida_DebeLanzar422() {
        // 1. GIVEN
        String email = "v@test.com";
        simularVendedor(email);

        CrearPublicacionRequest req = new CrearPublicacionRequest();
        req.setIdTipoInmueble(1);
        req.setIndicePortada(0);
        req.setCaracteristicasIds(List.of(99)); // ID 99 no permitido

        MultipartFile foto = mock(MultipartFile.class);
        
        Vendedor v = new Vendedor();
        TipoInmueble tipo = new TipoInmueble();
        tipo.setId(1);

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(v));
        when(tipos.findById(1)).thenReturn(Optional.of(tipo));
        when(listas.findByTipoInmuebleId(1)).thenReturn(new ArrayList<>()); // Lista vacía, nada permitido

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.crear(req, List.of(foto)));
        
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
        assertTrue(ex.getReason().contains("no pertenece al tipo seleccionado"));
    }

    // --- Pruebas para ACTUALIZAR ---

    @Test
    void actualizar_Exitoso_DebeActualizarYCambiarEstado() {
        // 1. GIVEN
        String email = "propietario@test.com";
        simularVendedor(email);
        Integer idPub = 50;

        Vendedor prop = new Vendedor();
        prop.setId(1);
        prop.setCorreo(email);

        Publicacion pub = new Publicacion();
        pub.setId(idPub);
        pub.setVendedor(prop); // Es dueño
        pub.setEstado("RECHAZADA");
        pub.setMotivoRechazo("Falta pintura");
        // Inicializar listas para evitar NullPointer
        pub.setCaracteristicas(new ArrayList<>());
        pub.setFotos(new ArrayList<>());
        pub.setDireccion(new Direccion());

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(prop));
        when(publicaciones.findById(idPub)).thenReturn(Optional.of(pub));

        UpdatePublicacionRequest req = new UpdatePublicacionRequest();
        req.setTitulo("Nuevo Titulo");
        req.setPrecio(5000.0);

        // 2. WHEN
        CrearPublicacionResponse res = service.actualizar(idPub, req, null);

        // 3. THEN
        assertEquals("PENDIENTE", pub.getEstado());
        assertNull(pub.getMotivoRechazo());
        assertEquals("Nuevo Titulo", pub.getTitulo());
        assertEquals(5000.0, pub.getPrecio());

        verify(publicaciones).save(pub);
        verify(movimientoRepository).save(any(Movimiento.class));
    }

    @Test
    void actualizar_NoEsDuenio_DebeLanzar403() {
        // 1. GIVEN
        String emailAtacante = "atacante@test.com";
        simularVendedor(emailAtacante);

        Vendedor atacante = new Vendedor();
        atacante.setId(2);
        atacante.setCorreo(emailAtacante);

        Vendedor duenio = new Vendedor();
        duenio.setId(1); // Diferente ID

        Publicacion pub = new Publicacion();
        pub.setId(10);
        pub.setVendedor(duenio);

        when(vendedores.findByCorreo(emailAtacante)).thenReturn(Optional.of(atacante));
        when(publicaciones.findById(10)).thenReturn(Optional.of(pub));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.actualizar(10, new UpdatePublicacionRequest(), null));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void actualizar_EliminarFotoNoPerteneciente_DebeLanzar403() {
        // 1. GIVEN
        String email = "yo@test.com";
        simularVendedor(email);

        Vendedor yo = new Vendedor();
        yo.setId(1);
        yo.setCorreo(email);

        Publicacion pub = new Publicacion();
        pub.setId(10);
        pub.setVendedor(yo);

        // Otra publicación ajena
        Publicacion pubAjena = new Publicacion();
        pubAjena.setId(20);

        FotoPublicacion fotoAjena = new FotoPublicacion();
        fotoAjena.setId(999);
        fotoAjena.setPublicacion(pubAjena); // La foto pertenece a la pub 20, no a la 10

        when(vendedores.findByCorreo(email)).thenReturn(Optional.of(yo));
        when(publicaciones.findById(10)).thenReturn(Optional.of(pub));
        when(fotosRepo.findById(999)).thenReturn(Optional.of(fotoAjena));

        UpdatePublicacionRequest req = new UpdatePublicacionRequest();
        req.setFotosEliminar(List.of(999));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.actualizar(10, req, null));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(fotoService, never()).eliminarFotoPublicacion(any());
    }
}