package com.inmapi.service;

import com.inmapi.modelo.FotoPerfil;
import com.inmapi.repository.FotoPerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FotoService {

    private final FotoPerfilRepository fotos;
    private final String relativePath = "uploads/fotos-perfil";

    private Path getUploadPathAbsolute() {
        return Paths.get(System.getProperty("user.dir"), this.relativePath);
    }

    public FotoPerfil guardar(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Foto de perfil requerida");
            }
            Path uploadPath = getUploadPathAbsolute();
            Files.createDirectories(uploadPath);
            String ext = obtenerExtension(file.getOriginalFilename());
            String nombreUnico = UUID.randomUUID().toString() + (ext.isBlank() ? "" : "." + ext);
            Path destino = uploadPath.resolve(nombreUnico);
            file.transferTo(destino);
            FotoPerfil fp = new FotoPerfil();
            String rutaRelativaParaDB = Paths.get(this.relativePath, nombreUnico).toString().replace("\\", "/");
            fp.setRuta(rutaRelativaParaDB); 
            
            return fotos.save(fp);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo guardar la foto", e);
        }
    }

    private String obtenerExtension(String nombre) {
        if (nombre == null) return "";
        int i = nombre.lastIndexOf('.');
        return (i == -1) ? "" : nombre.substring(i + 1);
    }
}

