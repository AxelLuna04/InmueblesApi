/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author HP
 */
public class EmailTemplatesTest {

    private final EmailTemplates templates = new EmailTemplates();

    @Test
    void verificacion_DebeContenerNombreYLink() {
        // 1. Datos de prueba
        String nombre = "Juan Pérez";
        String link = "http://localhost:8080/activar";

        // 2. Ejecución
        String html = templates.verificacion(nombre, link);
        String txt = templates.verificacionTxt(nombre, link);

        // 3. Verificación (HTML)
        assertNotNull(html);
        assertTrue(html.contains("Hola Juan Pérez"), "El HTML debe saludar al usuario");
        assertTrue(html.contains(link), "El HTML debe contener el enlace");
        assertTrue(html.contains("Verificar mi cuenta"), "El HTML debe tener el botón/texto clave");

        // 4. Verificación (Texto Plano)
        assertNotNull(txt);
        assertTrue(txt.contains("Hola Juan Pérez"));
        assertTrue(txt.contains(link));
    }

    @Test
    void verificacion_NombreNulo_NoDebeRomperse() {
        // Tu código tiene una protección: nombre == null ? "" : nombre
        // Probamos que funcione y no lance NullPointerException
        String html = templates.verificacion(null, "http://link.com");
        
        assertTrue(html.contains("Hola ,")); // Debe aparecer vacío, no "null"
    }

    @Test
    void confirmarCambioCorreo_DebeMostrarNuevoCorreo() {
        String nuevoCorreo = "nuevo@mail.com";
        String link = "http://link.com";
        
        String html = templates.confirmarCambioCorreo("Maria", link, nuevoCorreo);
        
        assertTrue(html.contains("<b>nuevo@mail.com</b>")); // Verifica que el correo esté en negritas o presente
        assertTrue(html.contains(link));
    }

    @Test
    void publicacionRechazada_DebeIncluirMotivo() {
        String titulo = "Casa en la Playa";
        String motivo = "Faltan fotos del baño";
        
        String html = templates.publicacionRechazada("Vendedor", titulo, motivo);
        String txt = templates.publicacionRechazadaTxt("Vendedor", titulo, motivo);

        // Validamos que el motivo de rechazo sea visible para el usuario
        assertTrue(html.contains(motivo));
        assertTrue(html.contains(titulo));
        assertTrue(html.contains("rechazada"));
        
        assertTrue(txt.contains(motivo));
    }

    @Test
    void avisoEliminacion_DebeIndicarTipoUsuario() {
        String html = templates.avisoEliminacionCuenta("Pedro", "Vendedor");
        
        assertTrue(html.contains("Tu cuenta de Vendedor ha sido eliminada"));
    }
}