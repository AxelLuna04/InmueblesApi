package com.inmapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MediaUrlBuilder {

    @Value("${app.public.url}")
    private String baseUrl;

    public String construirUrl(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return null;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String ruta = rutaRelativa.startsWith("/") ? rutaRelativa.substring(1) : rutaRelativa;
        
        return base + "/" + ruta;
    }
}
