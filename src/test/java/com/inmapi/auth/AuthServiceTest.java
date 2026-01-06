/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.auth;

import com.inmapi.dto.LoginResponse;
import com.inmapi.modelo.UsuarioLoginView;
import com.inmapi.repository.UsuarioLoginRepository;
import com.inmapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UsuarioLoginRepository users;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtService jwt;

    @InjectMocks
    private AuthService service;

    // --- Pruebas de LOGIN ---

    @Test
    void login_Exitoso_DebeRetornarTokens() {
        // 1. GIVEN
        String email = "juan@test.com";
        String pass = "123456";
        String hash = "$2a$10$hashSimulado";

        // MOCKEAMOS la vista porque es inmutable (sin setters)
        UsuarioLoginView u = mock(UsuarioLoginView.class);
        when(u.getCorreo()).thenReturn(email);
        when(u.getContrasenia()).thenReturn(hash);
        when(u.getRol()).thenReturn("CLIENTE");
        when(u.getFechaVerificacion()).thenReturn(LocalDateTime.now()); // Verificado

        when(users.findByCorreo(email)).thenReturn(Optional.of(u));
        when(encoder.matches(pass, hash)).thenReturn(true);
        when(jwt.newAccess(email, "CLIENTE")).thenReturn("access_token");
        when(jwt.newRefresh(email)).thenReturn("refresh_token");

        // 2. WHEN
        LoginResponse response = service.login(email, pass);

        // 3. THEN
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("CLIENTE", response.getRol());
    }

    @Test
    void login_UsuarioNoExiste_DebeLanzar401() {
        when(users.findByCorreo(anyString())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.login("noexiste@test.com", "123"));
        
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_ContraseniaIncorrecta_DebeLanzar401() {
        String hash = "hashReal";
        UsuarioLoginView u = mock(UsuarioLoginView.class);
        when(u.getContrasenia()).thenReturn(hash);
        
        when(users.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        when(encoder.matches("incorrecta", hash)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.login("juan@test.com", "incorrecta"));
        
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_CuentaNoVerificada_DebeLanzar403() {
        UsuarioLoginView u = mock(UsuarioLoginView.class);
        when(u.getContrasenia()).thenReturn("hash");
        when(u.getFechaVerificacion()).thenReturn(null); // NO VERIFICADO

        when(users.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        when(encoder.matches("123", "hash")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.login("juan@test.com", "123"));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // --- Pruebas de REFRESH ---

    @Test
    void refresh_Exitoso_DebeDarNuevoAccessToken() {
        String token = "refresh_valido";
        String email = "juan@test.com";

        Authentication authMock = new UsernamePasswordAuthenticationToken(email, null);
        when(jwt.toAuth(token)).thenReturn(Optional.of(authMock));

        UsuarioLoginView u = mock(UsuarioLoginView.class);
        when(u.getCorreo()).thenReturn(email);
        when(u.getRol()).thenReturn("CLIENTE");
        when(u.getFechaVerificacion()).thenReturn(LocalDateTime.now()); // Verificado
        
        when(users.findByCorreo(email)).thenReturn(Optional.of(u));
        when(jwt.newAccess(email, "CLIENTE")).thenReturn("nuevo_access");

        LoginResponse res = service.refresh(token);

        assertEquals("nuevo_access", res.getAccessToken());
        assertEquals(token, res.getRefreshToken());
    }

    @Test
    void refresh_UsuarioNoVerificado_DebeLanzar403() {
        String token = "valid_refresh";
        String email = "juan@test.com";
        
        Authentication authMock = new UsernamePasswordAuthenticationToken(email, null);
        when(jwt.toAuth(token)).thenReturn(Optional.of(authMock));

        UsuarioLoginView u = mock(UsuarioLoginView.class);
        when(u.getFechaVerificacion()).thenReturn(null); // NO VERIFICADO
        
        when(users.findByCorreo(email)).thenReturn(Optional.of(u));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> service.refresh(token));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}