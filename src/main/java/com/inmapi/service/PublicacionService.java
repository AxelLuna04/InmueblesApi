package com.inmapi.service;

import com.inmapi.dto.CrearPublicacionRequest;
import com.inmapi.dto.CrearPublicacionResponse;
import com.inmapi.dto.DireccionDTO;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final VendedorRepository vendedores;
    private final TipoInmuebleRepository tipos;
    private final DireccionRepository direcciones;
    private final PublicacionRepository publicaciones;
    private final ListaCaracteristicasRepository listas;
    private final CaracteristicaRepository caracteristicas;
    private final CaracteristicaSeleccionadaRepository seleccionadas;
    private final FotoPublicacionRepository fotosRepo;
    private final FotoService fotoService;

    private String emailActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (String) a.getPrincipal();
    }

    private boolean esVendedor() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_VENDEDOR"));
    }

    @Transactional
    public CrearPublicacionResponse crear(CrearPublicacionRequest dto, List<MultipartFile> fotos) {
        if (!esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo vendedores pueden publicar");
        }
        if (fotos == null || fotos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Debes subir al menos una foto");
        }
        if (dto.getIndicePortada() < 0 || dto.getIndicePortada() >= fotos.size()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Índice de portada inválido");
        }

        Vendedor vendedor = vendedores.findByCorreo(emailActual())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));

        TipoInmueble tipo = tipos.findById(dto.getIdTipoInmueble())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de inmueble no existe"));

        Set<Integer> permitidas = listas.findByTipoInmuebleId(tipo.getId()).stream()
                .map(lc -> lc.getCaracteristica().getId())
                .collect(java.util.stream.Collectors.toSet());

        List<Integer> pedidas = Optional.ofNullable(dto.getCaracteristicasIds()).orElse(List.of());
        for (Integer idc : pedidas) {
            if (!permitidas.contains(idc)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "La característica " + idc + " no pertenece al tipo seleccionado");
            }
        }

        Direccion direccion = mapearDireccion(dto.getDireccion());
        direcciones.save(direccion);

        Publicacion p = new Publicacion();
        p.setVendedor(vendedor);
        p.setTitulo(dto.getTitulo());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecio(dto.getPrecio());
        p.setNumeroHabitaciones(dto.getNumeroHabitaciones());
        p.setNumeroBanosCompletos(dto.getNumeroBanosCompletos());
        p.setNumeroExcusados(dto.getNumeroExcusados());
        p.setTipoInmueble(tipo);
        p.setDireccion(direccion);
        p.setTipoOperacion(dto.getTipoOperacion());

        publicaciones.save(p);

        if (!pedidas.isEmpty()) {
            for (Integer idc : pedidas) {
                Caracteristica c = caracteristicas.findById(idc)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Característica no existe"));
                CaracteristicaSeleccionada cs = new CaracteristicaSeleccionada();
                cs.setCaracteristica(c);
                cs.setPublicacion(p);
                seleccionadas.save(cs);
            }
        }

        for (int i = 0; i < fotos.size(); i++) {
            boolean esPortada = (i == dto.getIndicePortada());
            fotoService.guardarFotoPublicacion(fotos.get(i), p, esPortada);
        }

        return new CrearPublicacionResponse(p.getId(), "PENDIENTE", "Publicación creada y enviada a revisión");
    }

    private Direccion mapearDireccion(DireccionDTO d) {
        Direccion dir = new Direccion();
        dir.setFormattedAddress(d.getFormattedAddress());
        dir.setLine1(d.getLine1());
        dir.setSublocality(d.getSublocality());
        dir.setLocality(d.getLocality());
        dir.setAdminArea2(d.getAdminArea2());
        dir.setAdminArea1(d.getAdminArea1());
        dir.setPostalCode(d.getPostalCode());
        dir.setCountryCode(d.getCountryCode());
        dir.setLat(d.getLat());
        dir.setLng(d.getLng());
        dir.setProvider(d.getProvider());
        dir.setProviderPlaceId(d.getProviderPlaceId());
        return dir;
    }
}

