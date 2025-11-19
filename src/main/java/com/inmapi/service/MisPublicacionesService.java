package com.inmapi.service;

import com.inmapi.dto.DireccionDTO;
import com.inmapi.dto.MisPubCard;
import com.inmapi.dto.PublicacionDetalle;
import com.inmapi.modelo.FotoPublicacion;
import com.inmapi.modelo.Publicacion;
import com.inmapi.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class MisPublicacionesService {

  private final PublicacionRepository publicaciones;
  private final MediaUrlBuilder urlBuilder;

  private String emailActual() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || a.getPrincipal() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return (String) a.getPrincipal();
  }

  private boolean esVendedor() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_VENDEDOR"));
  }
  
  public Page<MisPubCard> listar(int page, int size) {
    if (!esVendedor()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

    Pageable pageable = PageRequest.of(Math.max(0,page), Math.min(size,50));
    var correo = emailActual();

    var pageEntity = publicaciones.findByVendedorCorreoOrderByCreadoEnDesc(correo, pageable);

    return pageEntity.map(p -> {
      var portada = p.getFotos().stream()
          .filter(FotoPublicacion::isEsPortada)
          .map(FotoPublicacion::getRuta)
          .map(urlBuilder::construirUrl)
          .findFirst().orElse(null);
      var tipoTxt = p.getTipoInmueble() != null ? p.getTipoInmueble().getTipo() : null;
      return new MisPubCard(
          p.getId(), p.getTitulo(), p.getEstado(), p.getPrecio(), tipoTxt, portada, p.getCreadoEn()
      );
    });
  }

  public PublicacionDetalle obtenerDetalleMio(Integer id) {
    if (!esVendedor()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    String correo = emailActual();

    Publicacion p = publicaciones.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

    if (!p.getVendedor().getCorreo().equalsIgnoreCase(correo)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a esta publicación");
    }

    DireccionDTO d = null;
    if (p.getDireccion() != null) {
      d = new DireccionDTO();
      d.setFormattedAddress(p.getDireccion().getFormattedAddress());
      d.setLine1(p.getDireccion().getLine1());
      d.setSublocality(p.getDireccion().getSublocality());
      d.setLocality(p.getDireccion().getLocality());
      d.setAdminArea2(p.getDireccion().getAdminArea2());
      d.setAdminArea1(p.getDireccion().getAdminArea1());
      d.setPostalCode(p.getDireccion().getPostalCode());
      d.setCountryCode(p.getDireccion().getCountryCode());
      d.setLat(p.getDireccion().getLat());
      d.setLng(p.getDireccion().getLng());
      d.setProvider(p.getDireccion().getProvider());
      d.setProviderPlaceId(p.getDireccion().getProviderPlaceId());
    }

    var fotos = p.getFotos().stream()
        .map(FotoPublicacion::getRuta)
        .map(urlBuilder::construirUrl)
        .toList();

    var caracts = p.getCaracteristicas().stream()
        .map(cs -> cs.getCaracteristica().getCaracteristica())
        .collect(Collectors.toList());

    return new PublicacionDetalle(
        p.getId(),
        p.getTitulo(),
        p.getDescripcion(),
        p.getPrecio(),
        p.getNumeroHabitaciones(),
        p.getNumeroBanosCompletos(),
        p.getNumeroExcusados(),
        p.getTipoOperacion(),
        p.getTipoInmueble() != null ? p.getTipoInmueble().getTipo() : null,
        d,
        fotos,
        caracts,
        p.getVendedor() != null ? p.getVendedor().getNombreCompleto() : null
    );
  }
}

