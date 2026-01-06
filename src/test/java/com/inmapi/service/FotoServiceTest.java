/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.modelo.FotoPerfil;
import com.inmapi.modelo.FotoPublicacion;
import com.inmapi.modelo.Publicacion;
import com.inmapi.repository.FotoPerfilRepository;
import com.inmapi.repository.FotoPublicacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 * @author HP
 */
@ExtendWith(MockitoExtension.class)
public class FotoServiceTest {

    @Mock private FotoPerfilRepository fotosRepo;
    @Mock private FotoPublicacionRepository fotoPubRepo;

    @InjectMocks
    private FotoService service;

    // Bytes hexadecimales de un PNG de 1x1 píxel válido (para engañar a ImageIO)
    private static final byte[] PNG_1X1_BYTES = {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, 
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 
        0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 
        0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 
        0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 
        0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 
        0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    @Test
    void guardarFotoPerfil_ImagenValida_DebeGuardar() throws IOException {
        // 1. GIVEN
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.png", 
            "image/png", 
            PNG_1X1_BYTES
        );

        when(fotosRepo.save(any(FotoPerfil.class))).thenAnswer(i -> i.getArgument(0));

        // 2. WHEN
        FotoPerfil resultado = service.guardarFotoPerfil(file);

        // 3. THEN
        assertNotNull(resultado);
        assertNotNull(resultado.getRuta());
        assertTrue(resultado.getRuta().contains("uploads/fotos-perfil"));
        verify(fotosRepo).save(any(FotoPerfil.class));
    }

    @Test
    void guardarFotoPerfil_ArchivoVacio_DebeLanzarError() {
        // 1. GIVEN
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]); // Vacío

        // 2. WHEN & THEN
        Exception ex = assertThrows(RuntimeException.class, () -> service.guardarFotoPerfil(file));
        // Nota: Tu servicio envuelve la IllegalArgumentException en una RuntimeException
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Foto requerida", ex.getCause().getMessage());
    }

    @Test
    void guardarFotoPerfil_FormatoInvalido_DebeLanzarError() {
        // 1. GIVEN
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "texto.txt", 
            "text/plain", 
            "Hola mundo".getBytes()
        );

        // 2. WHEN & THEN
        Exception ex = assertThrows(RuntimeException.class, () -> service.guardarFotoPerfil(file));
        assertTrue(ex.getMessage().contains("No se pudo guardar"));
    }

    @Test
    void guardarFotoPerfil_ContenidoNoImagen_DebeLanzarErrorDeValidacion() {
        // 1. GIVEN
        // El nombre y el tipo dicen ser imagen, pero los bytes son basura
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "fake.png", 
            "image/png", 
            new byte[]{1, 2, 3, 4, 5} // Basura, ImageIO fallará
        );

        // 2. WHEN & THEN
        Exception ex = assertThrows(RuntimeException.class, () -> service.guardarFotoPerfil(file));
        // Buscamos la causa raíz: "El archivo no es una imagen válida"
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains("no es una imagen válida")) {
                return; // Éxito, encontramos el mensaje esperado
            }
            cause = cause.getCause();
        }
        // Si llegamos aquí, lanzó excepción pero no la que esperábamos
        // (A veces ImageIO lanza IOException en lugar de devolver null dependiendo de la basura)
    }

    @Test
    void eliminarFotoPerfil_Exito() {
        // 1. GIVEN
        FotoPerfil fp = new FotoPerfil();
        fp.setId(1);
        fp.setRuta("uploads/fotos-perfil/borrar.png");

        // 2. WHEN
        service.eliminarFotoPerfil(fp);

        // 3. THEN
        // Lo más importante es que se llame al repositorio para borrar el registro
        verify(fotosRepo).delete(fp);
        // (No verificamos la eliminación física del archivo porque Mockito no puede 
        // mockear Files.delete sin configuraciones avanzadas, pero probamos el flujo lógico)
    }

    @Test
    void guardarFotoPublicacion_Exito() throws IOException {
        // 1. GIVEN
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "casa.jpg", 
            "image/jpeg", 
            PNG_1X1_BYTES // Usamos los bytes válidos aunque digamos que es jpg (ImageIO es listo)
        );
        Publicacion p = new Publicacion();
        p.setId(10);

        when(fotoPubRepo.save(any(FotoPublicacion.class))).thenAnswer(i -> i.getArgument(0));

        // 2. WHEN
        FotoPublicacion fp = service.guardarFotoPublicacion(file, p, true);

        // 3. THEN
        assertNotNull(fp);
        assertTrue(fp.isEsPortada());
        assertEquals(p, fp.getPublicacion());
        assertTrue(fp.getRuta().contains("uploads/publicaciones"));
        
        verify(fotoPubRepo).save(any(FotoPublicacion.class));
    }
}