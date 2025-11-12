package com.inmapi.service;

import com.inmapi.dto.PerfilResponse;
import com.inmapi.dto.UpdatePerfilClienteRequest;
import com.inmapi.dto.UpdatePerfilVendedorRequest;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final ClienteRepository clientes;
    private final VendedorRepository vendedores;
    private final OcupacionRepository ocupaciones;
    private final FotoPerfilRepository fotos;
    private final FotoService fotoService;

    private String emailActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (String) a.getPrincipal();
    }

    private boolean esRol(String rolEsperado) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) {
            return false;
        }
        return a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_" + rolEsperado));
    }

    private PerfilResponse toResponse(Cliente c) {
        return new PerfilResponse(
                "CLIENTE",
                c.getId(),
                c.getCorreo(),
                c.getNombreCompleto(),
                c.getPresupuesto(),
                c.getUbicacionInteres(),
                c.getNumeroMiembrosFamilia(),
                c.getOcupacion() == null ? null : c.getOcupacion().getId(),
                null,
                c.getFotoPerfil() == null ? null : c.getFotoPerfil().getId(),
                c.getFotoPerfil() == null ? null : c.getFotoPerfil().getRuta()
        );
    }

    private PerfilResponse toResponse(Vendedor v) {
        return new PerfilResponse(
                "VENDEDOR",
                v.getId(),
                v.getCorreo(),
                v.getNombreCompleto(),
                null,
                null,
                null,
                null,
                v.getTelefono(),
                v.getFotoPerfil() == null ? null : v.getFotoPerfil().getId(),
                v.getFotoPerfil() == null ? null : v.getFotoPerfil().getRuta()
        );
    }

    public PerfilResponse getPerfil() {
        String correo = emailActual();
        if (esRol("CLIENTE")) {
            var c = clientes.findByCorreo(correo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
            return toResponse(c);
        } else if (esRol("VENDEDOR")) {
            var v = vendedores.findByCorreo(correo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
            return toResponse(v);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
    }

    public PerfilResponse patchCliente(UpdatePerfilClienteRequest req) {
        if (!esRol("CLIENTE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        String correo = emailActual();

        var c = clientes.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        if (req.getNombreCompleto() != null) {
            c.setNombreCompleto(req.getNombreCompleto());
        }
        if (req.getPresupuesto() != null) {
            c.setPresupuesto(req.getPresupuesto());
        }
        if (req.getUbicacionInteres() != null) {
            c.setUbicacionInteres(req.getUbicacionInteres());
        }
        if (req.getNumeroMiembrosFamilia() != null) {
            c.setNumeroMiembrosFamilia(req.getNumeroMiembrosFamilia());
        }

        if (req.getIdOcupacion() != null) {
            if (req.getIdOcupacion() == -1) {
                //Aqui si se borra la ocupacion mandar del cliente un -1 para indicar eso.
                c.setOcupacion(null);
            } else {
                var oc = ocupaciones.findById(req.getIdOcupacion())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ocupación no existe"));
                c.setOcupacion(oc);
            }
        }

        var saved = clientes.save(c);
        return toResponse(saved);
    }

    public PerfilResponse patchVendedor(UpdatePerfilVendedorRequest req) {
        if (!esRol("VENDEDOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        String correo = emailActual();

        var v = vendedores.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));

        if (req.getNombreCompleto() != null) {
            v.setNombreCompleto(req.getNombreCompleto());
        }
        if (req.getTelefono() != null) {
            v.setTelefono(req.getTelefono());
        }

        var saved = vendedores.save(v);
        return toResponse(saved);
    }

    public PerfilResponse cambiarFoto(MultipartFile foto) {
        String correo = emailActual();

        if (esRol("CLIENTE")) {
            var c = clientes.findByCorreo(correo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
            FotoPerfil nueva = fotoService.guardar(foto);
            c.setFotoPerfil(nueva);
            clientes.save(c);
            return toResponse(c);
        } else if (esRol("VENDEDOR")) {
            var v = vendedores.findByCorreo(correo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
            FotoPerfil nueva = fotoService.guardar(foto);
            v.setFotoPerfil(nueva);
            vendedores.save(v);
            return toResponse(v);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
    }
}

