/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.auth;

import com.inmapi.dto.RegistroRequest;
import com.inmapi.dto.RegistroResponse;
import com.inmapi.modelo.Cliente;
import com.inmapi.modelo.FotoPerfil;
import com.inmapi.modelo.UsuarioLoginView;
import com.inmapi.modelo.Vendedor;
import com.inmapi.repository.ClienteRepository;
import com.inmapi.repository.UsuarioLoginRepository;
import com.inmapi.repository.VendedorRepository;
import com.inmapi.service.EmailService;
import com.inmapi.service.EmailTemplates;
import com.inmapi.service.VerificationLinkBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class RegistroServiceTest {

    @Mock private ClienteRepository clientes;
    @Mock private VendedorRepository vendedores;
    @Mock private UsuarioLoginRepository loginView;
    @Mock private PasswordEncoder encoder;
    @Mock private EmailService email; // El servicio mockeado se llama 'email'
    @Mock private EmailTemplates templates;
    @Mock private VerificationLinkBuilder linkBuilder;

    @InjectMocks
    private RegistroService service;

    @BeforeEach
    void setup() {
        // Configuramos mocks comunes si es necesario
    }

    // Método auxiliar para obtener el tipo/rol sin importar cómo se llame en el DTO
    private String obtenerTipoUsuario(RegistroResponse res) {
        try {
            return (String) res.getClass().getMethod("getTipoUsuario").invoke(res);
        } catch (Exception e1) {
            try {
                return (String) res.getClass().getMethod("getRol").invoke(res);
            } catch (Exception e2) {
                try {
                    return (String) res.getClass().getMethod("getTipo").invoke(res);
                } catch (Exception e3) {
                    return "DESCONOCIDO";
                }
            }
        }
    }

    // --- Pruebas de REGISTRO ---

    @Test
    void registrar_ClienteExito_DebeGuardarYEnviarCorreo() {
        // 1. GIVEN
        RegistroRequest req = new RegistroRequest();
        req.setCorreo("nuevo@test.com");
        req.setContrasenia("123");
        req.setNombreCompleto("Nuevo Cliente");
        req.setTipoUsuario("CLIENTE");

        // El correo NO existe
        when(loginView.findByCorreo("nuevo@test.com")).thenReturn(Optional.empty());
        when(encoder.encode("123")).thenReturn("hash123");
        when(linkBuilder.buildVerifyUrl(anyString())).thenReturn("http://link");
        
        // Mock de guardado (devuelve ID)
        when(clientes.save(any(Cliente.class))).thenAnswer(i -> {
            Cliente c = i.getArgument(0);
            c.setId(10);
            return c;
        });

        // 2. WHEN
        RegistroResponse res = service.registrar(req, null);

        // 3. THEN
        assertNotNull(res);
        // Usamos el método auxiliar para evitar error de compilación
        assertEquals("CLIENTE", obtenerTipoUsuario(res)); 
        assertEquals(10, res.getId());
        
        verify(clientes).save(any(Cliente.class)); // Se guardó
        verify(email).enviar(eq("nuevo@test.com"), anyString(), any(), any()); // Se envió correo
    }

    @Test
    void registrar_VendedorExito() {
        // 1. GIVEN
        RegistroRequest req = new RegistroRequest();
        req.setCorreo("vendedor@test.com");
        req.setContrasenia("abc");
        req.setNombreCompleto("Vendedor Top");
        req.setTipoUsuario("VENDEDOR");
        req.setTelefono("555-1234");

        when(loginView.findByCorreo("vendedor@test.com")).thenReturn(Optional.empty());
        when(vendedores.save(any(Vendedor.class))).thenAnswer(i -> {
            Vendedor v = i.getArgument(0);
            v.setId(20);
            return v;
        });

        // 2. WHEN
        RegistroResponse res = service.registrar(req, new FotoPerfil());

        // 3. THEN
        assertEquals("VENDEDOR", obtenerTipoUsuario(res));
        verify(vendedores).save(any(Vendedor.class));
        verify(email).enviar(eq("vendedor@test.com"), anyString(), any(), any());
    }

    @Test
    void registrar_CorreoDuplicado_DebeLanzarConflict() {
        // 1. GIVEN
        RegistroRequest req = new RegistroRequest();
        req.setCorreo("existe@test.com");

        // El correo SÍ existe (usamos mock de la vista)
        UsuarioLoginView vistaMock = mock(UsuarioLoginView.class);
        when(loginView.findByCorreo("existe@test.com")).thenReturn(Optional.of(vistaMock));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.registrar(req, null));
        
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Ese correo ya está registrado", ex.getReason());
    }

    @Test
    void registrar_TipoInvalido_DebeLanzarBadRequest() {
        // 1. GIVEN
        RegistroRequest req = new RegistroRequest();
        req.setCorreo("a@b.com");
        req.setTipoUsuario("HACKER"); // Tipo desconocido

        when(loginView.findByCorreo(anyString())).thenReturn(Optional.empty());

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.registrar(req, null));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- Pruebas de VERIFICACIÓN ---

    @Test
    void verificar_TokenValido_Cliente() {
        // 1. GIVEN
        String token = "tok123";
        Cliente c = new Cliente();
        c.setTokenVerificacion(token);
        c.setExpiracionToken(LocalDateTime.now().plusHours(1)); // Válido

        when(clientes.findByTokenVerificacion(token)).thenReturn(Optional.of(c));

        // 2. WHEN
        String msg = service.verificar(token);

        // 3. THEN
        assertEquals("Cliente verificado", msg);
        assertNotNull(c.getFechaVerificacion()); // Se marcó verificado
        assertNull(c.getTokenVerificacion()); // Se limpió el token
        verify(clientes).save(c);
    }

    @Test
    void verificar_TokenExpirado_DebeLanzar410() {
        // 1. GIVEN
        String token = "expired";
        Vendedor v = new Vendedor();
        v.setTokenVerificacion(token);
        v.setExpiracionToken(LocalDateTime.now().minusMinutes(5)); // Expirado

        when(clientes.findByTokenVerificacion(token)).thenReturn(Optional.empty());
        when(vendedores.findByTokenVerificacion(token)).thenReturn(Optional.of(v));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.verificar(token));
        
        assertEquals(HttpStatus.GONE, ex.getStatusCode());
    }

    @Test
    void verificar_TokenNoExiste_DebeLanzar404() {
        // 1. GIVEN
        String token = "fake";
        when(clientes.findByTokenVerificacion(token)).thenReturn(Optional.empty());
        when(vendedores.findByTokenVerificacion(token)).thenReturn(Optional.empty());

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.verificar(token));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- Pruebas de REENVIAR VERIFICACIÓN ---

    @Test
    void reenviarVerificacion_ClienteNoVerificado_DebeGenerarNuevoToken() {
        // 1. GIVEN
        String correoPrueba = "c@test.com"; // RENOMBRADO para no chocar con 'this.email'
        
        Cliente c = new Cliente();
        c.setCorreo(correoPrueba);
        c.setFechaVerificacion(null); // No verificado

        // Simulamos findAll().stream().filter...
        when(clientes.findAll()).thenReturn(List.of(c));
        when(linkBuilder.buildVerifyUrl(anyString())).thenReturn("http://newlink");

        // 2. WHEN
        service.reenviarVerificacion(correoPrueba);

        // 3. THEN
        assertNotNull(c.getTokenVerificacion()); // Nuevo token generado
        verify(clientes).save(c);
        
        // Ahora sí usamos 'email' (el mock) y 'correoPrueba' (el string)
        verify(email).enviar(eq(correoPrueba), anyString(), any(), any());
    }
}