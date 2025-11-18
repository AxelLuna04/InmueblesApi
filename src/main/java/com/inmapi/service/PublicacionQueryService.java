package com.inmapi.service;

import com.inmapi.dto.*;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import com.inmapi.spec.PublicacionSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacionQueryService {

    private final PublicacionRepository publicaciones;
    private final FotoPublicacionRepository fotos;
    private final ClienteRepository clientes;

    private String emailActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getPrincipal() != null ? (String) a.getPrincipal() : null;
    }
    
    private boolean esCliente() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_CLIENTE"));
    }

    public Page<PublicacionCard> paraTi(int page, int size) {
        if (!esCliente()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String correo = emailActual();
        Cliente cliente = clientes.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        Specification<Publicacion> specBase = Specification.where(PublicacionSpecs.estadoAprobada());

        if (cliente.getOcupacion() != null) {
            String oc = cliente.getOcupacion().getNombre().toLowerCase();
            if (oc.contains("estudiante") || oc.contains("becario")) {
                specBase = specBase.and(PublicacionSpecs.operacion("RENTA"));
            }
        }

        List<Publicacion> candidatos = publicaciones.findAll(specBase, Sort.by(Sort.Direction.DESC, "creadoEn"));
        List<PublicacionCard> recomendados = candidatos.stream()
            .map(p -> {
                int puntaje = calcularPuntaje(p, cliente);
                return new Pair(p, puntaje);
            })
            .filter(pair -> pair.score > 0)
            .sorted((a, b) -> Integer.compare(b.score, a.score))
            .map(pair -> toCard(pair.publicacion))
            .collect(Collectors.toList());

        int start = Math.min((int)PageRequest.of(page, size).getOffset(), recomendados.size());
        int end = Math.min((start + size), recomendados.size());
        
        List<PublicacionCard> pageContent = recomendados.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, size), recomendados.size());
    }

    private int calcularPuntaje(Publicacion p, Cliente c) {
        int score = 0;

        if (c.getUbicacionInteres() != null && p.getDireccion() != null) {
            String interes = c.getUbicacionInteres().toLowerCase();
            String direccionCasa = p.getDireccion().getFormattedAddress().toLowerCase();
            if (direccionCasa.contains(interes)) {
                score += 50;
            }
        }

        if (c.getPresupuesto() != null && c.getPresupuesto() > 0) {
            double precio = p.getPrecio();
            double presupuesto = c.getPresupuesto();

            if (precio <= presupuesto) {
                score += 40;
            } else if (precio <= (presupuesto * 1.20)) {
                score += 10;
            } else {
                score -= 10;
            }
        }

        if (c.getNumeroMiembrosFamilia() != null) {
            try {
                int miembros = Integer.parseInt(c.getNumeroMiembrosFamilia());
                int habitacionesNecesarias = Math.max(1, miembros - 1);
                
                if (p.getNumeroHabitaciones() != null && p.getNumeroHabitaciones() >= habitacionesNecesarias) {
                    score += 30;
                }
            } catch (Exception ignored) {}
        }

        if (c.getOcupacion() != null) {
            String oc = c.getOcupacion().getNombre().toLowerCase();
            if (oc.contains("estudiante")) {
                if ("RENTA".equals(p.getTipoOperacion())) score += 10;
                if (p.getPrecio() < 5000) score += 20;
            }
        }
        
        return score;
    }
    
    private static class Pair {
        Publicacion publicacion;
        int score;
        public Pair(Publicacion p, int s) { this.publicacion = p; this.score = s; }
    }

    public Page<PublicacionCard> listar(PublicacionFiltro f, int page, int size, String sort) {
        Sort s = switch (sort == null ? "recientes" : sort) {
            case "precio-asc"  -> Sort.by(Sort.Direction.ASC, "precio");
            case "precio-desc" -> Sort.by(Sort.Direction.DESC, "precio");
            default            -> Sort.by(Sort.Direction.DESC, "creadoEn");
        };
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(size, 50), s);

        List<Integer> carIds = parseIds(f.getCaracteristicas());

        Specification<Publicacion> spec = Specification
            .where(PublicacionSpecs.estadoAprobada())
            .and(PublicacionSpecs.tipo(f.getIdTipoInmueble()))
            .and(PublicacionSpecs.operacion(f.getTipoOperacion())) 
            .and(PublicacionSpecs.precioMin(f.getPrecioMin()))
            .and(PublicacionSpecs.precioMax(f.getPrecioMax()))
            .and(PublicacionSpecs.habMin(f.getHabMin()))
            .and(PublicacionSpecs.banosMin(f.getBanosMin()))
            .and(PublicacionSpecs.excusadosMin(f.getExcusadosMin()))
            .and(PublicacionSpecs.texto(f.getQ()))
            .and(PublicacionSpecs.ubicacion(f.getUbicacion()))
            .and(PublicacionSpecs.conTodasCaracts(carIds));

        Page<Publicacion> pageResult = publicaciones.findAll(spec, pageable);
        return pageResult.map(this::toCard);
    }

    private List<Integer> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
            .map(String::trim).filter(s -> !s.isEmpty())
            .map(Integer::valueOf).collect(Collectors.toList());
    }

    private PublicacionCard toCard(Publicacion p) {
        String portada = fotos.findFirstByPublicacionIdAndEsPortadaTrue(p.getId())
            .map(FotoPublicacion::getRuta).orElse(null);
        String dirCorta = p.getDireccion() != null ? p.getDireccion().getFormattedAddress() : null;
        String tipoTxt = p.getTipoInmueble() != null ? p.getTipoInmueble().getTipo() : null;
        
        return new PublicacionCard(
            p.getId(),
            p.getTitulo(),
            p.getPrecio(),
            dirCorta,
            p.getNumeroHabitaciones(),
            p.getNumeroBanosCompletos(),
            p.getNumeroExcusados(),
            portada,
            tipoTxt
        );
    }
    
    public PublicacionDetalle detalle(Integer id) {
        Publicacion p = publicaciones.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
        if (!"APROBADA".equals(p.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No disponible");
        }
        var fotosRutas = p.getFotos().stream().map(FotoPublicacion::getRuta).toList();
        var carNombres = p.getCaracteristicas().stream().map(cs -> cs.getCaracteristica().getCaracteristica()).toList();
        var tipoTxt = p.getTipoInmueble() != null ? p.getTipoInmueble().getTipo() : null;
    
        DireccionDTO d = new DireccionDTO();
        var dir = p.getDireccion();
        if (dir != null) {
          d.setFormattedAddress(dir.getFormattedAddress());
          d.setLine1(dir.getLine1());
          d.setSublocality(dir.getSublocality());
          d.setLocality(dir.getLocality());
          d.setAdminArea2(dir.getAdminArea2());
          d.setAdminArea1(dir.getAdminArea1());
          d.setPostalCode(dir.getPostalCode());
          d.setCountryCode(dir.getCountryCode());
          d.setLat(dir.getLat());
          d.setLng(dir.getLng());
          d.setProvider(dir.getProvider());
          d.setProviderPlaceId(dir.getProviderPlaceId());
        }
    
        return new PublicacionDetalle(
            p.getId(),
            p.getTitulo(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getNumeroHabitaciones(),
            p.getNumeroBanosCompletos(),
            p.getNumeroExcusados(),
            tipoTxt,
            d,
            fotosRutas,
            carNombres,
            p.getVendedor() != null ? p.getVendedor().getNombreCompleto() : null
        );
    }
}