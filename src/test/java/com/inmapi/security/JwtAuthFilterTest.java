/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private Authentication authMock;

    @InjectMocks
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        // Limpiamos el contexto de seguridad antes de cada prueba para evitar contaminación
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Limpiamos después también por buena práctica
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_TokenValido_DebeAutenticarYContinuar() throws ServletException, IOException {
        // 1. GIVEN
        String token = "tokenValido123";
        String header = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(header);
        // Simulamos que el JwtService convierte el token en una Autenticación válida
        when(jwtService.toAuth(token)).thenReturn(Optional.of(authMock));

        // 2. WHEN
        // Llamamos a doFilter (método público) que internamente llama a doFilterInternal
        filter.doFilter(request, response, chain);

        // 3. THEN
        // Verificamos que se estableció la autenticación en el contexto global
        Authentication authResultado = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authResultado, "La autenticación no debería ser nula");
        assertEquals(authMock, authResultado);

        // Verificamos que la cadena de filtros continúe
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_SinHeader_DebeContinuarSinAutenticar() throws ServletException, IOException {
        // 1. GIVEN
        when(request.getHeader("Authorization")).thenReturn(null);

        // 2. WHEN
        filter.doFilter(request, response, chain);

        // 3. THEN
        // El contexto debe seguir vacío
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // Pero la cadena debe continuar (para que pueda llegar al login o a rutas públicas)
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService); // No debió intentar validar nada
    }

    @Test
    void doFilter_HeaderSinBearer_DebeContinuarSinAutenticar() throws ServletException, IOException {
        // 1. GIVEN
        // Header existe pero no empieza con "Bearer "
        when(request.getHeader("Authorization")).thenReturn("Basic user:pass");

        // 2. WHEN
        filter.doFilter(request, response, chain);

        // 3. THEN
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_TokenInvalido_DebeContinuarSinAutenticar() throws ServletException, IOException {
        // 1. GIVEN
        String token = "tokenInvalido";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        // Simulamos que el servicio dice "Empty" (token caducado o falso)
        when(jwtService.toAuth(token)).thenReturn(Optional.empty());

        // 2. WHEN
        filter.doFilter(request, response, chain);

        // 3. THEN
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}