package com.inmapi.service;

import com.inmapi.dto.CrearPublicacionRequest;
import com.inmapi.dto.CrearPublicacionResponse;
import com.inmapi.dto.DireccionDTO;
import com.inmapi.dto.UpdatePublicacionRequest;
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

    @Transactional
    public CrearPublicacionResponse actualizar(Integer idPublicacion,
            UpdatePublicacionRequest dto,
            List<MultipartFile> fotosNuevas) {
        if (!esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo vendedores pueden editar");
        }

        var vendedor = vendedores.findByCorreo(emailActual())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));

        var p = publicaciones.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no existe"));

        if (!p.getVendedor().getId().equals(vendedor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar esta publicación");
        }

        if (dto.getTitulo() != null) {
            p.setTitulo(dto.getTitulo());
        }
        if (dto.getDescripcion() != null) {
            p.setDescripcion(dto.getDescripcion());
        }
        if (dto.getTipoOperacion() != null) {
            p.setTipoOperacion(dto.getTipoOperacion());
        }
        if (dto.getPrecio() != null) {
            p.setPrecio(dto.getPrecio());
        }

        if (dto.getNumeroHabitaciones() != null) {
            p.setNumeroHabitaciones(dto.getNumeroHabitaciones());
        }
        if (dto.getNumeroBanosCompletos() != null) {
            p.setNumeroBanosCompletos(dto.getNumeroBanosCompletos());
        }
        if (dto.getNumeroExcusados() != null) {
            p.setNumeroExcusados(dto.getNumeroExcusados());
        }

        Integer tipoId = (dto.getIdTipoInmueble() != null)
                ? dto.getIdTipoInmueble()
                : (p.getTipoInmueble() != null ? p.getTipoInmueble().getId() : null);

        if (dto.getIdTipoInmueble() != null) {
            var tipo = tipos.findById(dto.getIdTipoInmueble())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de inmueble no existe"));
            p.setTipoInmueble(tipo);
        }

        if (dto.getCaracteristicasIds() != null) {
            var permitidas = listas.findByTipoInmuebleId(tipoId).stream()
                    .map(lc -> lc.getCaracteristica().getId())
                    .collect(java.util.stream.Collectors.toSet());

            for (Integer idc : dto.getCaracteristicasIds()) {
                if (!permitidas.contains(idc)) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "La característica " + idc + " no pertenece al tipo seleccionado");
                }
            }

            seleccionadas.deleteAll(p.getCaracteristicas());
            p.getCaracteristicas().clear();

            for (Integer idc : dto.getCaracteristicasIds()) {
                var c = caracteristicas.findById(idc)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Característica no existe"));
                var cs = new CaracteristicaSeleccionada();
                cs.setCaracteristica(c);
                cs.setPublicacion(p);
                seleccionadas.save(cs);
                p.getCaracteristicas().add(cs);
            }
        }

        if (dto.getDireccion() != null) {
            var d = dto.getDireccion();
            var dir = p.getDireccion();
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
            direcciones.save(dir);
        }

        if (dto.getFotosEliminar() != null && !dto.getFotosEliminar().isEmpty()) {
            for (Integer idFoto : dto.getFotosEliminar()) {
                var foto = fotosRepo.findById(idFoto)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no existe: " + idFoto));
                if (!foto.getPublicacion().getId().equals(p.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes borrar esta foto");
                }

                fotoService.eliminarFotoPublicacion(foto);
            }
        }

        if (fotosNuevas != null && !fotosNuevas.isEmpty()) {
            for (MultipartFile f : fotosNuevas) {
                fotoService.guardarFotoPublicacion(f, p, false);
            }
        }

        if (dto.getPortadaId() != null) {
            var todas = p.getFotos();
            boolean found = false;
            for (var f : todas) {
                boolean es = f.getId().equals(dto.getPortadaId());
                if (es && !f.getPublicacion().getId().equals(p.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Portada inválida");
                }
                f.setEsPortada(es);
                if (es) {
                    found = true;
                }
                fotosRepo.save(f);
            }
            if (!found) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "portadaId no pertenece a esta publicación");
            }
        } else {
            var tienePortada = p.getFotos().stream().anyMatch(FotoPublicacion::isEsPortada);
            if (!tienePortada && !p.getFotos().isEmpty()) {
                var f0 = p.getFotos().get(0);
                f0.setEsPortada(true);
                fotosRepo.save(f0);
            }
        }

        p.setEstado("PENDIENTE");

        try {
            var field = Publicacion.class.getDeclaredField("motivoRechazo");
            field.setAccessible(true);
            field.set(p, null);
        } catch (Exception ignored) {
        }

        publicaciones.save(p);

        return new CrearPublicacionResponse(p.getId(), p.getEstado(), "Publicación actualizada. Enviada a revisión");
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

