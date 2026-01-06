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
public class VerificationLinkBuilderTest {

    private VerificationLinkBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new VerificationLinkBuilder();
    }

    @Test
    void buildVerifyUrl_BaseSinSlash_DebeConcatenarCorrectamente() {
        // 1. GIVEN
        ReflectionTestUtils.setField(builder, "publicApiUrl", "http://api.com");
        String token = "xyz123";

        // 2. WHEN
        String url = builder.buildVerifyUrl(token);

        // 3. THEN
        assertEquals("http://api.com/v1/auth/verify?token=xyz123", url);
    }

    @Test
    void buildVerifyUrl_BaseConSlash_DebeRemoverSlashDuplicado() {
        // 1. GIVEN
        // Simulamos que el usuario puso la url con / al final en el properties
        ReflectionTestUtils.setField(builder, "publicApiUrl", "http://api.com/");
        String token = "abc999";

        // 2. WHEN
        String url = builder.buildVerifyUrl(token);

        // 3. THEN
        // La lógica del servicio debe quitar el slash final de la base antes de concatenar
        assertEquals("http://api.com/v1/auth/verify?token=abc999", url);
    }

    @Test
    void buildConfirmEmailChangeUrl_Exito() {
        // 1. GIVEN
        ReflectionTestUtils.setField(builder, "publicApiUrl", "http://localhost:8080");
        String token = "token-cambio";

        // 2. WHEN
        String url = builder.buildConfirmEmailChangeUrl(token);

        // 3. THEN
        assertEquals("http://localhost:8080/v1/auth/confirm-email-change?token=token-cambio", url);
    }
}