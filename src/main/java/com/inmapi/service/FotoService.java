package com.inmapi.service;

import com.inmapi.modelo.FotoPerfil;
import com.inmapi.modelo.FotoPublicacion;
import com.inmapi.modelo.Publicacion;
import com.inmapi.repository.FotoPerfilRepository;
import com.inmapi.repository.FotoPublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FotoService {

    private final FotoPerfilRepository fotos;
    private final FotoPublicacionRepository fotoPublicacionRepository;

    private static final String PERFIL_PATH = "uploads/fotos-perfil";
    private static final String PUBLICACION_PATH = "uploads/publicaciones";
    private static final Path ROOT_DIR = Paths.get(System.getProperty("user.dir"));

    public FotoPerfil guardarFotoPerfil(MultipartFile file) {
        try {
            String rutaRelativa = guardarArchivo(file, PERFIL_PATH);

            FotoPerfil fp = new FotoPerfil();
            fp.setRuta(rutaRelativa);
            return fotos.save(fp);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo guardar la foto de perfil", e);
        }
    }

    public void eliminarFotoPerfil(FotoPerfil foto) {
        if (foto == null) {
            return;
        }
        eliminarArchivo(foto.getRuta());
        fotos.delete(foto);
    }
    
    public void eliminarFotoPublicacion(FotoPublicacion foto) {
        if (foto == null) {
            return;
        }
        eliminarArchivo(foto.getRuta());
        fotoPublicacionRepository.delete(foto);
    }

    public FotoPublicacion guardarFotoPublicacion(MultipartFile file, Publicacion p, boolean esPortada) {
        try {
            String rutaRelativa = guardarArchivo(file, PUBLICACION_PATH);

            // 2. Crea y guarda la entidad de Publicación
            FotoPublicacion fp = new FotoPublicacion();
            fp.setRuta(rutaRelativa);
            fp.setPublicacion(p);
            fp.setEsPortada(esPortada);
            return fotoPublicacionRepository.save(fp);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo guardar la foto de publicación", e);
        }
    }

    private String guardarArchivo(MultipartFile file, String relativePath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Foto requerida");
        }

        Path uploadPath = ROOT_DIR.resolve(relativePath);
        Files.createDirectories(uploadPath);

        String ext = obtenerExtension(file.getOriginalFilename());
        String nombreUnico = UUID.randomUUID().toString() + (ext.isBlank() ? "" : "." + ext);
        Path destino = uploadPath.resolve(nombreUnico);

        file.transferTo(destino);
        return Paths.get(relativePath, nombreUnico).toString().replace("\\", "/");
    }

    private void eliminarArchivo(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        try {
            Path rutaAbsoluta = ROOT_DIR.resolve(rutaRelativa);
            Files.deleteIfExists(rutaAbsoluta);
        } catch (Exception e) {
            System.err.println("No se pudo eliminar el archivo de foto: " + rutaRelativa);
            e.printStackTrace();
        }
    }

    private String obtenerExtension(String nombre) {
        if (nombre == null) {
            return "";
        }
        int i = nombre.lastIndexOf('.');
        return (i == -1) ? "" : nombre.substring(i + 1);
    }
}

