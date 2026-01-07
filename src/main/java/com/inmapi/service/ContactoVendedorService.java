/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.service;

import com.inmapi.dto.ContactoVendedorResponse;
import com.inmapi.modelo.AccesoVendedor;
import com.inmapi.modelo.Cliente;
import com.inmapi.modelo.Publicacion;
import com.inmapi.modelo.Vendedor;
import com.inmapi.repository.AccesoVendedorRepository;
import com.inmapi.repository.ClienteRepository;
import com.inmapi.repository.PublicacionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ContactoVendedorService {

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

    

    public ContactoVendedorResponse obtenerContactoVendedor(Integer idPublicacion) {
        if (!esCliente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo clientes pueden ver datos de contacto");
        }

        Cliente cliente = clienteActual();

        Publicacion pub = publicaciones.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        
        var accesoOpt = accesos.findByClienteIdAndPublicacionId(cliente.getId(), pub.getId());
        if (accesoOpt.isEmpty()) {
            
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Debes realizar el pago para ver los datos de contacto del vendedor"
            );
        }

        Vendedor vendedor = pub.getVendedor();

        return new ContactoVendedorResponse(
                vendedor.getId(),
                vendedor.getNombreCompleto(),
                vendedor.getCorreo(),
                vendedor.getTelefono()
        );
    }
    
    public List<ContactoVendedorResponse> listarVendedoresDesbloqueados() {
        if (!esCliente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo clientes pueden ver su historial de contactos.");
        }

        Cliente cliente = clienteActual();

        
        List<AccesoVendedor> misAccesos = accesos.findByClienteId(cliente.getId());

        
        return misAccesos.stream()
                .map(acceso -> acceso.getPublicacion().getVendedor())
                .distinct()
                .map(vendedor -> new ContactoVendedorResponse(
                        vendedor.getId(),
                        vendedor.getNombreCompleto(),
                        vendedor.getCorreo(),
                        vendedor.getTelefono()
                ))
                .toList();
    }
}

