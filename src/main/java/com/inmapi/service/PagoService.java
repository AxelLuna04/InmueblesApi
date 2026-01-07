/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.RealizarPagoRequest;
import com.inmapi.dto.RealizarPagoResponse;
import com.inmapi.dto.TipoPagoResponse;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final TipoPagoRepository tiposPago;
    private final AccesoVendedorRepository accesos;
    private final ClienteRepository clientes;
    private final PublicacionRepository publicaciones;


    private String emailActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (String) a.getPrincipal();
    }

    private boolean esCliente() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_CLIENTE"));
    }

    private Cliente clienteActual() {
        String correo = emailActual();
        return clientes.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }


    public List<TipoPagoResponse> obtenerTiposPago() {
        return tiposPago.findAll().stream()
                .map(t -> new TipoPagoResponse(t.getId(), t.getTipoPago()))
                .toList();
    }


    @Transactional
    public RealizarPagoResponse pagarAcceso(Integer idPublicacion, RealizarPagoRequest req) {
        if (!esCliente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo clientes pueden realizar pagos");
        }

        if (req.getMonto() == null || req.getMonto() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Monto inválido");
        }

        Cliente cliente = clienteActual();

        Publicacion pub = publicaciones.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        var accesoExistenteOpt = accesos.findByClienteIdAndPublicacionId(cliente.getId(), pub.getId());
        if (accesoExistenteOpt.isPresent()) {
            AccesoVendedor acceso = accesoExistenteOpt.get();
            String nombreTipo = acceso.getTipoPago() != null ? acceso.getTipoPago().getTipoPago() : null;

            return new RealizarPagoResponse(
                    true,                         
                    true,                        
                    acceso.getId(),               
                    nombreTipo,
                    acceso.getMonto(),
                    "Ya contabas con acceso a los datos del vendedor para esta publicación."
            );
        }

        
        TipoPago tipoPago = tiposPago.findById(req.getIdTipoPago())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Tipo de pago inválido"));

        
        if (!simularPagoExterno(tipoPago, req)) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Pago rechazado por la pasarela simulada");
        }

        
        AccesoVendedor acceso = new AccesoVendedor();
        acceso.setCliente(cliente);
        acceso.setPublicacion(pub);
        acceso.setTipoPago(tipoPago);
        acceso.setMonto(req.getMonto());
        acceso.setFechaPago(LocalDateTime.now());
        acceso.setSimulacionDatos(req.getDatosSimulados());

        accesos.save(acceso);

        return new RealizarPagoResponse(
                true,                       
                false,                      
                acceso.getId(),
                tipoPago.getTipoPago(),
                acceso.getMonto(),
                "Pago realizado con éxito. Ya puedes ver los datos de contacto del vendedor."
        );
    }

   
    private boolean simularPagoExterno(TipoPago tipo, RealizarPagoRequest req) {
        
        if (req.getDatosSimulados() == null || req.getDatosSimulados().isBlank()) {
            return false;
        }
        return true;
    }
}

