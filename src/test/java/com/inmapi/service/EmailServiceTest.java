/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        // Inyectamos el valor de @Value("${spring.mail.from}") manualmente
        ReflectionTestUtils.setField(emailService, "from", "no-reply@test.com");
        
        // Configuramos el mock para que devuelva nuestro mensaje simulado
        // Lenient es necesario porque no todos los tests usan createMimeMessage
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void enviarHtml_Exito_DebeEnviarMensaje() {
        // 1. GIVEN
        String para = "usuario@test.com";
        String asunto = "Bienvenido";
        String html = "<h1>Hola</h1>";

        // 2. WHEN
        emailService.enviarHtml(para, asunto, html);

        // 3. THEN
        // Verificamos que se creó el mensaje y se intentó enviar
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void enviarHtml_FalloEnEnvio_DebeLanzarRuntimeException() {
        // 1. GIVEN
        // Simulamos que el sender falla
        doThrow(new MailSendException("Error SMTP")).when(mailSender).send(any(MimeMessage.class));

        // 2. WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class, 
            () -> emailService.enviarHtml("a@b.com", "Subject", "Body"));
        
        assertEquals("No se pudo enviar el correo", ex.getMessage());
    }

    @Test
    void enviar_Exito_DebeEnviarMultipart() {
        // 1. GIVEN
        String para = "usuario@test.com";
        String asunto = "Aviso";
        String html = "<p>Html</p>";
        String txt = "Texto plano";

        // 2. WHEN
        emailService.enviar(para, asunto, html, txt);

        // 3. THEN
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void enviar_FalloEnEnvio_DebeLanzarRuntimeException() {
        // 1. GIVEN
        doThrow(new MailSendException("Error Conexión")).when(mailSender).send(any(MimeMessage.class));

        // 2. WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class, 
            () -> emailService.enviar("a@b.com", "Sub", "html", "txt"));
        
        assertEquals("No se pudo enviar el correo", ex.getMessage());
    }
}