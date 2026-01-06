/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.ChangeEmailRequest;
import com.inmapi.dto.ChangePasswordRequest;
import com.inmapi.modelo.Cliente;
import com.inmapi.modelo.FotoPerfil;
import com.inmapi.modelo.UsuarioLoginView;
import com.inmapi.modelo.Vendedor;
import com.inmapi.repository.ClienteRepository;
import com.inmapi.repository.UsuarioLoginRepository;
import com.inmapi.repository.VendedorRepository;
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
public class CuentaServiceTest {

    @Mock private ClienteRepository clientes;
    @Mock private VendedorRepository vendedores;
    @Mock private UsuarioLoginRepository loginView;
    @Mock private PasswordEncoder encoder;
    @Mock private FotoService fotoService;
    @Mock private EmailService email;
    @Mock private EmailTemplates templates;
    @Mock private VerificationLinkBuilder links;

    @InjectMocks
    private CuentaService service;

    // --- Helpers de Seguridad ---
    private void simularCliente(String email) {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        var auth = new UsernamePasswordAuthenticationToken(email, null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void simularVendedor(String email) {
        var auths = List.of(new SimpleGrantedAuthority("ROLE_VENDEDOR"));
        var auth = new UsernamePasswordAuthenticationToken(email, null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    // --- 1. Cambio de Contraseña ---

    @Test
    void cambiarContrasenia_Exito_Cliente() {
        // 1. GIVEN
        String mail = "c@test.com";
        simularCliente(mail);
        
        Cliente c = new Cliente();
        c.setCorreo(mail);
        c.setContrasenia("hash_viejo");

        when(clientes.findByCorreo(mail)).thenReturn(Optional.of(c));
        when(encoder.matches("123456", "hash_viejo")).thenReturn(true);
        when(encoder.encode("nueva123")).thenReturn("hash_nuevo");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setActual("123456");
        req.setNueva("nueva123");

        // 2. WHEN
        service.cambiarContrasenia(req);

        // 3. THEN
        verify(clientes).save(c); // Se guardó
        assertEquals("hash_nuevo", c.getContrasenia()); // Se actualizó el hash
        verify(email).enviar(eq(mail), anyString(), any(), any()); // Se envió correo
    }

    @Test
    void cambiarContrasenia_MismaContrasenia_Error422() {
        // 1. GIVEN
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setActual("123456");
        req.setNueva("123456"); // Iguales

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.cambiarContrasenia(req));
        
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void cambiarContrasenia_PassIncorrecta_Error401() {
        // 1. GIVEN
        String mail = "v@test.com";
        simularVendedor(mail);
        
        Vendedor v = new Vendedor();
        v.setContrasenia("hash_real");
        
        when(vendedores.findByCorreo(mail)).thenReturn(Optional.of(v));
        when(encoder.matches("incorrecta", "hash_real")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setActual("incorrecta");
        req.setNueva("nueva123");

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.cambiarContrasenia(req));
        
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    // --- 2. Solicitud Cambio de Correo ---

    @Test
    void solicitarCambioCorreo_Exito_GeneraToken() {
        // 1. GIVEN
        String mailActual = "old@test.com";
        String mailNuevo = "new@test.com";
        simularCliente(mailActual);

        Cliente c = new Cliente();
        c.setCorreo(mailActual);

        when(loginView.findByCorreo(mailNuevo)).thenReturn(Optional.empty()); // Correo libre
        when(clientes.findByCorreo(mailActual)).thenReturn(Optional.of(c));
        when(links.buildConfirmEmailChangeUrl(anyString())).thenReturn("http://link");

        ChangeEmailRequest req = new ChangeEmailRequest();
        req.setNuevoCorreo(mailNuevo);

        // 2. WHEN
        service.solicitarCambioCorreo(req);

        // 3. THEN
        assertNotNull(c.getEmailCambioToken());
        assertEquals(mailNuevo, c.getEmailNuevo());
        verify(clientes).save(c);
        verify(email).enviar(eq(mailNuevo), anyString(), any(), any());
    }

    @Test
    void solicitarCambioCorreo_CorreoOcupado_Error409() {
        // 1. GIVEN
        ChangeEmailRequest req = new ChangeEmailRequest();
        req.setNuevoCorreo("ocupado@test.com");

        when(loginView.findByCorreo("ocupado@test.com"))
            .thenReturn(Optional.of(new UsuarioLoginView()));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.solicitarCambioCorreo(req));
        
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // --- 3. Confirmar Cambio de Correo ---

    @Test
    void confirmarCambioCorreo_Exito_Vendedor() {
        // 1. GIVEN
        String token = "tok123";
        Vendedor v = new Vendedor();
        v.setEmailNuevo("new@vendedor.com");
        v.setEmailCambioToken(token);
        v.setEmailCambioExp(LocalDateTime.now().plusHours(1)); // Válido

        when(clientes.findByEmailCambioToken(token)).thenReturn(Optional.empty());
        when(vendedores.findByEmailCambioToken(token)).thenReturn(Optional.of(v));
        when(loginView.findByCorreo("new@vendedor.com")).thenReturn(Optional.empty()); // Libre

        // 2. WHEN
        String msg = service.confirmarCambioCorreo(token);

        // 3. THEN
        assertEquals("Correo de vendedor actualizado", msg);
        assertEquals("new@vendedor.com", v.getCorreo()); // Se actualizó
        assertNull(v.getEmailNuevo()); // Se limpió
        verify(vendedores).save(v);
    }

    @Test
    void confirmarCambioCorreo_TokenExpirado_Error410() {
        // 1. GIVEN
        String token = "expired";
        Cliente c = new Cliente();
        c.setEmailCambioExp(LocalDateTime.now().minusMinutes(10)); // Expirado

        when(clientes.findByEmailCambioToken(token)).thenReturn(Optional.of(c));

        // 2. WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.confirmarCambioCorreo(token));
        
        assertEquals(HttpStatus.GONE, ex.getStatusCode());
    }

    // --- 4. Eliminar Cuenta ---

    @Test
    void eliminarCuenta_Exito_ClienteConFoto() {
        // 1. GIVEN
        String mail = "bye@test.com";
        simularCliente(mail);
        String pass = "pass123";

        Cliente c = new Cliente();
        c.setCorreo(mail);
        c.setContrasenia("hash");
        FotoPerfil foto = new FotoPerfil();
        c.setFotoPerfil(foto);

        when(clientes.findByCorreo(mail)).thenReturn(Optional.of(c));
        when(encoder.matches(pass, "hash")).thenReturn(true);

        // 2. WHEN
        service.eliminarCuenta(pass);

        // 3. THEN
        verify(clientes).delete(c); // Se borró de BD
        verify(fotoService).eliminarFotoPerfil(foto); // Se borró foto física
        verify(email).enviar(eq(mail), anyString(), any(), any()); // Aviso final
    }
}