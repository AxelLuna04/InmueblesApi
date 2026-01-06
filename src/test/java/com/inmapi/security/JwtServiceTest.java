/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    // Clave secreta fuerte para HS256 (debe tener al menos 32 caracteres/256 bits)
    // Si usas una muy corta en la prueba, JJWT lanzará error de "Weak Key".
    private final String SECRET_KEY = "12345678901234567890123456789012"; 
    
    @BeforeEach
    void setUp() {
        // Inyectamos manualmente los valores de @Value
        ReflectionTestUtils.setField(jwtService, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessMinutes", 60L); // 1 hora
        ReflectionTestUtils.setField(jwtService, "refreshDays", 1L);
    }

    @Test
    void newAccess_DebeGenerarTokenValidoConRol() {
        // 1. GIVEN
        String correo = "admin@test.com";
        String rol = "ADMIN";

        // 2. WHEN
        String token = jwtService.newAccess(correo, rol);

        // 3. THEN
        assertNotNull(token);
        assertFalse(token.isBlank());

        // Validamos que podamos leerlo de vuelta
        Optional<Authentication> authOpt = jwtService.toAuth(token);
        assertTrue(authOpt.isPresent());
        
        Authentication auth = authOpt.get();
        assertEquals(correo, auth.getName()); // El subject es el correo
        
        // Verificamos que se haya agregado el prefijo ROLE_
        boolean tieneRol = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        assertTrue(tieneRol, "El token debe contener el rol ROLE_ADMIN");
    }

    @Test
    void newRefresh_DebeGenerarTokenValido() {
        // 1. GIVEN
        String correo = "user@test.com";

        // 2. WHEN
        String token = jwtService.newRefresh(correo);

        // 3. THEN
        assertNotNull(token);
        
        // Validamos lectura
        Optional<Authentication> authOpt = jwtService.toAuth(token);
        assertTrue(authOpt.isPresent());
        assertEquals(correo, authOpt.get().getName());
        
        // Si no especificamos rol en refresh, tu código pone "USER" por defecto en el catch/getOrDefault
        boolean tieneRolUser = authOpt.get().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        assertTrue(tieneRolUser);
    }

    @Test
    void toAuth_TokenInvalido_DebeRetornarEmpty() {
        // 1. GIVEN
        String tokenBasura = "esto.no.es.un.token";

        // 2. WHEN
        Optional<Authentication> resultado = jwtService.toAuth(tokenBasura);

        // 3. THEN
        assertTrue(resultado.isEmpty());
    }

    @Test
    void toAuth_TokenManipulado_DebeRetornarEmpty() {
        // 1. GIVEN: Generamos un token válido
        String token = jwtService.newAccess("hacker@test.com", "USER");
        
        // 2. WHEN: Le cambiamos un caracter al final (rompemos la firma)
        String tokenFalso = token.substring(0, token.length() - 5) + "aaaaa";

        // 3. THEN
        Optional<Authentication> resultado = jwtService.toAuth(tokenFalso);
        assertTrue(resultado.isEmpty(), "La firma no coincide, debe rechazarlo");
    }

    @Test
    void toAuth_TokenExpirado_DebeRetornarEmpty() throws InterruptedException {
        // 1. GIVEN: Generamos token manualmente con expiración casi inmediata (1 milisegundo)
        // Usamos el método genérico generate para controlar el TTL
        String token = jwtService.generate(
                "rapido@test.com", 
                Map.of("rol", "USER"), 
                Duration.ofMillis(1) 
        );

        // Esperamos un poquito para asegurar que expire (10ms es suficiente para 1ms de vida)
        Thread.sleep(10); 

        // 2. WHEN
        Optional<Authentication> resultado = jwtService.toAuth(token);

        // 3. THEN
        assertTrue(resultado.isEmpty(), "El token expiró, debe retornar empty");
    }
}