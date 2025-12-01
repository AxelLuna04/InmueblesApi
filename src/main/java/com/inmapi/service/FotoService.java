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

    private static final Set<String> MIME_PERMITIDOS = Set.of(
        "image/jpeg", "image/png", "image/webp"
    );
    private static final Set<String> EXT_PERMITIDAS = Set.of(
        "jpg","jpeg","png","webp"
    );
    private static final long MAX_BYTES = 10 * 1024 * 1024;

    private void validarImagen(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Foto requerida");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("La imagen excede el tamaño máximo (10MB).");
        }
        String contentType = file.getContentType();
        if (contentType == null || !MIME_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato de imagen no permitido. Usa JPG, PNG o WEBP.");
        }
        String ext = obtenerExtension(file.getOriginalFilename()).toLowerCase();
        if (!ext.isBlank() && !EXT_PERMITIDAS.contains(ext)) {
            throw new IllegalArgumentException("Extensión no permitida. Usa JPG, PNG o WEBP.");
        }
        
        try (var is = file.getInputStream()) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                throw new IllegalArgumentException("El archivo no es una imagen válida.");
            }
            
            if (img.getWidth() > 10000 || img.getHeight() > 10000) {
                throw new IllegalArgumentException("Dimensiones excesivas.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo validar la imagen.", e);
        }
    }

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
        validarImagen(file);

        Path uploadPath = ROOT_DIR.resolve(relativePath);
        Files.createDirectories(uploadPath);

        String extOriginal = obtenerExtension(file.getOriginalFilename()).toLowerCase();
        String ext;
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (ct.contains("jpeg")) ext = "jpg";
        else if (ct.contains("png")) ext = "png";
        else if (ct.contains("webp")) ext = "webp";
        else ext = extOriginal.isBlank() ? "jpg" : extOriginal;

        String nombreUnico = UUID.randomUUID().toString() + "." + ext;
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

