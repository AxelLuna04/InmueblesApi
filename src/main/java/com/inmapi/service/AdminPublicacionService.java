package com.inmapi.service;

import com.inmapi.dto.*;
import com.inmapi.modelo.FotoPublicacion;
import com.inmapi.modelo.Publicacion;
import com.inmapi.repository.FotoPublicacionRepository;
import com.inmapi.repository.PublicacionRepository;
import com.inmapi.spec.PublicacionSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPublicacionService {

    private final PublicacionRepository publicaciones;
    private final FotoPublicacionRepository fotos;
    private final EmailService email;
    private final EmailTemplates templates;
    private final MediaUrlBuilder urlBuilder;

    private boolean esAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
    }

    public Page<ModeracionCard> listar(ModeracionFiltro f, int page, int size) {
        if (!esAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var sort = Sort.by(Sort.Direction.DESC, "creadoEn");
        var pageable = PageRequest.of(Math.max(0, page), Math.min(size, 50), sort);

        var spec = Specification.where(PublicacionSpecs.porEstado(
                f.getEstado() == null ? "PENDIENTE" : f.getEstado()))
                .and(PublicacionSpecs.tipo(f.getTipo()))
                .and(PublicacionSpecs.texto(f.getQ()));

        return publicaciones.findAll(spec, pageable).map(p -> {
            var rutaDisco = fotos.findFirstByPublicacionIdAndEsPortadaTrue(p.getId())
                    .map(FotoPublicacion::getRuta).orElse(null);
            String urlWeb = urlBuilder.construirUrl(rutaDisco);
            var tipo = p.getTipoInmueble() != null ? p.getTipoInmueble().getTipo() : null;
            var dir = p.getDireccion() != null ? p.getDireccion().getFormattedAddress() : null;
            return new ModeracionCard(p.getId(), p.getTitulo(), p.getPrecio(), tipo, dir, urlWeb, p.getEstado(), p.getCreadoEn());
        });
    }

    public ModeracionDetalle detalle(Integer id) {
        if (!esAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var p = publicaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        var fotosList = p.getFotos().stream().map(FotoPublicacion::getRuta).map(urlBuilder::construirUrl).toList();
        var carNombres = p.getCaracteristicas().stream()
                .map(cs -> cs.getCaracteristica().getCaracteristica()).toList();

        var dir = p.getDireccion();
        return new ModeracionDetalle(
                p.getId(),
                p.getEstado(),
                p.getMotivoRechazo(),
                p.getTitulo(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getNumeroHabitaciones(),
                p.getNumeroBanosCompletos(),
                p.getNumeroExcusados(),
                p.getTipoInmueble() != null ? p.getTipoInmueble().getTipo() : null,
                p.getVendedor() != null ? p.getVendedor().getNombreCompleto() : null,
                p.getVendedor() != null ? p.getVendedor().getCorreo() : null,
                dir != null ? dir.getFormattedAddress() : null,
                dir != null ? dir.getLat() : null,
                dir != null ? dir.getLng() : null,
                p.getCreadoEn(),
                fotosList,
                carNombres
        );
    }

    @Transactional
    public ModeracionResponse aprobar(Integer id) {
        if (!esAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var p = publicaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        if ("APROBADA".equals(p.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya está aprobada");
        }
        p.setEstado("APROBADA");
        p.setMotivoRechazo(null);
        publicaciones.save(p);

        if (p.getVendedor() != null) {
            email.enviar(
                    p.getVendedor().getCorreo(),
                    "Tu publicación fue aprobada",
                    templates.publicacionAprobada(p.getVendedor().getNombreCompleto(), p.getTitulo()),
                    templates.publicacionAprobadaTxt(p.getVendedor().getNombreCompleto(), p.getTitulo())
            );
        }

        return new ModeracionResponse(p.getId(), p.getEstado(), "Publicación aprobada");
    }

    @Transactional
    public ModeracionResponse rechazar(Integer id, String motivo) {
        if (!esAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var p = publicaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        if ("RECHAZADA".equals(p.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya está rechazada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Motivo requerido");
        }
        p.setEstado("RECHAZADA");
        p.setMotivoRechazo(motivo);
        publicaciones.save(p);

        if (p.getVendedor() != null) {
            email.enviar(
                    p.getVendedor().getCorreo(),
                    "Tu publicación fue rechazada",
                    templates.publicacionRechazada(p.getVendedor().getNombreCompleto(), p.getTitulo(), motivo),
                    templates.publicacionRechazadaTxt(p.getVendedor().getNombreCompleto(), p.getTitulo(), motivo)
            );
        }

        return new ModeracionResponse(p.getId(), p.getEstado(), "Publicación rechazada");
    }
}

