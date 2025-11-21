/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.InteresadoResponse;
import com.inmapi.dto.VenderInmuebleRequest;
import com.inmapi.dto.VenderInmuebleResponse;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaInmuebleService {

    private final PublicacionRepository publicaciones;
    private final VendedorRepository vendedores;
    private final ClienteRepository clientes;
    private final AccesoVendedorRepository accesos;
    private final MovimientoRepository movimientos;
    private final ContratoRepository contratos;

    // ========= helpers de seguridad =========
    private String emailActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (String) a.getPrincipal();
    }

    private boolean esVendedor() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_VENDEDOR"));
    }

    private Vendedor vendedorActual() {
        String correo = emailActual();
        return vendedores.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
    }

    private Publicacion obtenerPublicacionPropia(Integer idPublicacion) {
        Vendedor vendedor = vendedorActual();
        Publicacion pub = publicaciones.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        if (!pub.getVendedor().getId().equals(vendedor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes vender una publicación que no es tuya");
        }

        return pub;
    }

    // ========= listar interesados =========
    public List<InteresadoResponse> listarInteresados(Integer idPublicacion) {
        if (!esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo vendedores pueden ver interesados");
        }

        Publicacion pub = obtenerPublicacionPropia(idPublicacion);

        List<AccesoVendedor> listaAccesos = accesos.findByPublicacionId(pub.getId());

        // evitar duplicados por cliente
        Map<Integer, Cliente> unicos = new LinkedHashMap<>();
        for (AccesoVendedor av : listaAccesos) {
            Cliente c = av.getCliente();
            if (c != null) {
                unicos.putIfAbsent(c.getId(), c);
            }
        }

        return unicos.values().stream()
                .map(c -> new InteresadoResponse(
                c.getId(),
                c.getNombreCompleto(),
                c.getCorreo(),
                c.getTelefono()
        ))
                .collect(Collectors.toList());
    }

    // ========= vender inmueble =========
    @Transactional
    public VenderInmuebleResponse venderInmueble(
            Integer idPublicacion,
            VenderInmuebleRequest req,
            MultipartFile documentoVenta
    ) {
        if (!esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo vendedores pueden vender un inmueble");
        }

        Publicacion pub = obtenerPublicacionPropia(idPublicacion);

        if ("VENDIDA".equalsIgnoreCase(pub.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta publicación ya está marcada como vendida");
        }

        // PRE: debe haber al menos un cliente con acceso pagado
        if (!accesos.existsByPublicacionId(pub.getId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No hay interesados registrados para esta publicación");
        }

        // 1) validar comprador
        Cliente comprador = clientes.findById(req.getIdClienteComprador())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente comprador no encontrado"));

        // 2) validar que el comprador realmente pagó acceso a esta publicación
        AccesoVendedor accesoComprador = accesos.findByClienteIdAndPublicacionId(comprador.getId(), pub.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "El cliente seleccionado no ha pagado acceso a este inmueble"));

        // 3) marcar publicación como VENDIDA (ya no se muestra en listados)
        pub.setEstado("VENDIDA");
        publicaciones.save(pub);

        // 4) registrar movimiento de VENTA en el historial
        Movimiento mov = new Movimiento();
        mov.setPublicacion(pub);
        mov.setTipoMovimiento("VENTA");
        mov.setFecha(req.getFechaVenta());
        mov.setArrendador(comprador); // reusamos este campo para el comprador
        movimientos.save(mov);

        // 5) registrar contrato / documento de venta (opcional)
        Integer idContrato = null;
        if (documentoVenta != null && !documentoVenta.isEmpty()) {
            Contrato contrato = new Contrato();
            contrato.setPublicacion(pub);
            contrato.setCliente(comprador);
            contrato.setFechaCarga(req.getFechaVenta());
            contrato.setTipoPago(accesoComprador.getTipoPago()); // usamos el tipo de pago del acceso

            // Aquí faltaría la lógica real para guardar el archivo en disco / S3, etc.
            // De momento guardamos el nombre como ruta relativa de ejemplo.
            contrato.setRutaDocumento(documentoVenta.getOriginalFilename());

            contratos.save(contrato);
            idContrato = contrato.getId();
        }

        return new VenderInmuebleResponse(
                pub.getId(),
                pub.getEstado(),
                mov.getId(),
                idContrato,
                "Inmueble vendido correctamente y registrado en el historial."
        );
    }
}
