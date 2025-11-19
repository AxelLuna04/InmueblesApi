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

    // ===== helpers de seguridad =====

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

    // ===== listar tipos de pago =====

    public List<TipoPagoResponse> obtenerTiposPago() {
        return tiposPago.findAll().stream()
                .map(t -> new TipoPagoResponse(t.getId(), t.getTipoPago()))
                .toList();
    }

    // ===== realizar pago y registrar acceso =====

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

        // 1) ¿Ya tiene acceso?
        var accesoExistenteOpt = accesos.findByClienteIdAndPublicacionId(cliente.getId(), pub.getId());
        if (accesoExistenteOpt.isPresent()) {
            AccesoVendedor acceso = accesoExistenteOpt.get();
            String nombreTipo = acceso.getTipoPago() != null ? acceso.getTipoPago().getTipoPago() : null;

            return new RealizarPagoResponse(
                    true,                         // exito
                    true,                         // yaTeniaAcceso
                    acceso.getId(),               // idAcceso
                    nombreTipo,
                    acceso.getMonto(),
                    "Ya contabas con acceso a los datos del vendedor para esta publicación."
            );
        }

        // 2) Validar tipo de pago
        TipoPago tipoPago = tiposPago.findById(req.getIdTipoPago())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Tipo de pago inválido"));

        // 3) Simular la pasarela de pago
        if (!simularPagoExterno(tipoPago, req)) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Pago rechazado por la pasarela simulada");
        }

        // 4) Registrar el acceso
        AccesoVendedor acceso = new AccesoVendedor();
        acceso.setCliente(cliente);
        acceso.setPublicacion(pub);
        acceso.setTipoPago(tipoPago);
        acceso.setMonto(req.getMonto());
        acceso.setFechaPago(LocalDateTime.now());
        acceso.setSimulacionDatos(req.getDatosSimulados());

        accesos.save(acceso);

        return new RealizarPagoResponse(
                true,                       // exito
                false,                      // yaTeniaAcceso
                acceso.getId(),
                tipoPago.getTipoPago(),
                acceso.getMonto(),
                "Pago realizado con éxito. Ya puedes ver los datos de contacto del vendedor."
        );
    }

    // ===== simulación muy sencilla de pasarela =====
    private boolean simularPagoExterno(TipoPago tipo, RealizarPagoRequest req) {
        // Aquí podrías hacer validaciones más específicas según tipo de pago.
        // Para la simulación, mientras tenga datosSimulados y monto>0 lo consideramos aprobado.
        if (req.getDatosSimulados() == null || req.getDatosSimulados().isBlank()) {
            return false;
        }
        // Podrías usar tipo.getTipoPago() para decidir otras reglas si quieres.
        return true;
    }
}

