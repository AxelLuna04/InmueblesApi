package com.inmapi.service;

import com.inmapi.dto.ChangeEmailRequest;
import com.inmapi.dto.ChangePasswordRequest;
import com.inmapi.modelo.Cliente;
import com.inmapi.modelo.Vendedor;
import com.inmapi.repository.ClienteRepository;
import com.inmapi.repository.UsuarioLoginRepository;
import com.inmapi.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final ClienteRepository clientes;
    private final VendedorRepository vendedores;
    private final UsuarioLoginRepository loginView;
    private final PasswordEncoder encoder;

    private final EmailService email;
    private final EmailTemplates templates;
    private final VerificationLinkBuilder links;

    private String emailActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (String) a.getPrincipal();
    }

    private boolean esCliente() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_CLIENTE"));
    }

    private boolean esVendedor() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_VENDEDOR"));
    }

    public void cambiarContrasenia(ChangePasswordRequest req) {
        if (req.getActual().equals(req.getNueva())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La nueva contraseña no puede ser igual a la actual");
        }

        String correo = emailActual();
        if (esCliente()) {
            Cliente c = clientes.findByCorreo(correo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
            if (!encoder.matches(req.getActual(), c.getContrasenia())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña actual incorrecta");
            }

            c.setContrasenia(encoder.encode(req.getNueva()));
            clientes.save(c);

            email.enviar(
                    c.getCorreo(),
                    "Tu contraseña fue actualizada",
                    templates.avisoCambioContrasenia(c.getNombreCompleto()),
                    templates.avisoCambioContraseniaTxt(c.getNombreCompleto())
            );
            return;
        }

        if (esVendedor()) {
            Vendedor v = vendedores.findByCorreo(correo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
            if (!encoder.matches(req.getActual(), v.getContrasenia())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña actual incorrecta");
            }

            v.setContrasenia(encoder.encode(req.getNueva()));
            vendedores.save(v);

            email.enviar(
                    v.getCorreo(),
                    "Tu contraseña fue actualizada",
                    templates.avisoCambioContrasenia(v.getNombreCompleto()),
                    templates.avisoCambioContraseniaTxt(v.getNombreCompleto())
            );
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
    }

    public void solicitarCambioCorreo(ChangeEmailRequest req) {
        String nuevo = req.getNuevoCorreo().toLowerCase(Locale.ROOT).trim();
        
        if (loginView.findByCorreo(nuevo).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está en uso");
        }

        String correoActual = emailActual();
        String token = UUID.randomUUID().toString();
        LocalDateTime exp = LocalDateTime.now().plusHours(24);

        if (esCliente()) {
            Cliente c = clientes.findByCorreo(correoActual)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
            c.setEmailCambioToken(token);
            c.setEmailNuevo(nuevo);
            c.setEmailCambioExp(exp);
            clientes.save(c);

            String link = links.buildConfirmEmailChangeUrl(token);
            email.enviar(
                    nuevo,
                    "Confirma tu nuevo correo",
                    templates.confirmarCambioCorreo(c.getNombreCompleto(), link, nuevo),
                    templates.confirmarCambioCorreoTxt(c.getNombreCompleto(), link, nuevo)
            );
            return;
        }

        if (esVendedor()) {
            Vendedor v = vendedores.findByCorreo(correoActual)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
            v.setEmailCambioToken(token);
            v.setEmailNuevo(nuevo);
            v.setEmailCambioExp(exp);
            vendedores.save(v);

            String link = links.buildConfirmEmailChangeUrl(token);
            email.enviar(
                    nuevo,
                    "Confirma tu nuevo correo",
                    templates.confirmarCambioCorreo(v.getNombreCompleto(), link, nuevo),
                    templates.confirmarCambioCorreoTxt(v.getNombreCompleto(), link, nuevo)
            );
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
    }

    public String confirmarCambioCorreo(String token) {
        var cOpt = clientes.findByEmailCambioToken(token);
        if (cOpt.isPresent()) {
            var c = cOpt.get();
            if (c.getEmailCambioExp() != null && c.getEmailCambioExp().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.GONE, "Token expirado");
            }
            if (c.getEmailNuevo() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud inválida");
            }

            if (loginView.findByCorreo(c.getEmailNuevo()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está en uso");
            }

            c.setCorreo(c.getEmailNuevo());
            c.setEmailNuevo(null);
            c.setEmailCambioToken(null);
            c.setEmailCambioExp(null);
            clientes.save(c);
            return "Correo de cliente actualizado";
        }

        var vOpt = vendedores.findByEmailCambioToken(token);
        if (vOpt.isPresent()) {
            var v = vOpt.get();
            if (v.getEmailCambioExp() != null && v.getEmailCambioExp().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.GONE, "Token expirado");
            }
            if (v.getEmailNuevo() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud inválida");
            }

            if (loginView.findByCorreo(v.getEmailNuevo()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está en uso");
            }

            v.setCorreo(v.getEmailNuevo());
            v.setEmailNuevo(null);
            v.setEmailCambioToken(null);
            v.setEmailCambioExp(null);
            vendedores.save(v);
            return "Correo de vendedor actualizado";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no válido");
    }
}

