/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author HP
 */
public class MediaUrlBuilderTest {

    private MediaUrlBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new MediaUrlBuilder();
        // Simulamos que en application.properties dice: app.public.url=http://localhost:8080
        ReflectionTestUtils.setField(builder, "baseUrl", "http://localhost:8080");
    }

    @Test
    void construirUrl_EntradaValida_DebeConcatenarCorrectamente() {
        // 1. GIVEN
        String rutaRelativa = "uploads/fotos/casa.jpg";

        // 2. WHEN
        String resultado = builder.construirUrl(rutaRelativa);

        // 3. THEN
        assertEquals("http://localhost:8080/uploads/fotos/casa.jpg", resultado);
    }

    @Test
    void construirUrl_BaseConSlashYRutaConSlash_DebeEvitarDobleSlash() {
        // 1. GIVEN
        // Cambiamos la base para que termine en /
        ReflectionTestUtils.setField(builder, "baseUrl", "http://miweb.com/");
        // La ruta empieza con /
        String rutaRelativa = "/perfil/avatar.png";

        // 2. WHEN
        String resultado = builder.construirUrl(rutaRelativa);

        // 3. THEN
        // La lógica de tu clase debe quitar los slash sobrantes y dejar solo uno en medio
        assertEquals("http://miweb.com/perfil/avatar.png", resultado);
    }

    @Test
    void construirUrl_NullOVacio_DebeRetornarNull() {
        assertNull(builder.construirUrl(null));
        assertNull(builder.construirUrl(""));
        assertNull(builder.construirUrl("   "));
    }
    
    @Test
    void construirUrl_BaseSinSlashRutaSinSlash_DebeAgregarSlash() {
        // GIVEN
        ReflectionTestUtils.setField(builder, "baseUrl", "http://api.com");
        String ruta = "img.jpg";
        
        // WHEN
        String res = builder.construirUrl(ruta);
        
        // THEN
        assertEquals("http://api.com/img.jpg", res);
    }
}