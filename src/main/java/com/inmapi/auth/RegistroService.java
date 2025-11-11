package com.inmapi.auth;

import com.inmapi.dto.RegistroRequest;
import com.inmapi.dto.RegistroResponse;
import com.inmapi.modelo.*;
import com.inmapi.repository.*;
import com.inmapi.service.EmailService;
import com.inmapi.service.EmailTemplates;
import com.inmapi.service.VerificationLinkBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final ClienteRepository clientes;
    private final VendedorRepository vendedores;
    private final UsuarioLoginRepository loginView;
    private final PasswordEncoder encoder;
    private final EmailService email;
    private final EmailTemplates templates;
    private final VerificationLinkBuilder linkBuilder;

    public RegistroResponse registrar(RegistroRequest req, FotoPerfil fotoGuardada) {
        String correo = req.getCorreo().toLowerCase(Locale.ROOT).trim();

        if (loginView.findByCorreo(correo).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expira = ahora.plusHours(24);

        switch (req.getTipoUsuario().toUpperCase(Locale.ROOT)) {
            case "CLIENTE" -> {
                var c = new Cliente();
                c.setCorreo(correo);
                c.setContrasenia(encoder.encode(req.getContrasenia()));
                c.setNombreCompleto(req.getNombreCompleto());
                c.setFechaNacimiento(req.getFechaNacimiento());
                c.setPresupuesto(null);
                c.setUbicacionInteres(null);
                c.setNumeroMiembrosFamilia(null);
                c.setOcupacion(null);
                c.setFotoPerfil(fotoGuardada);
                c.setTokenVerificacion(token);
                c.setFechaVerificacion(null);
                c.setExpiracionToken(expira);

                var saved = clientes.save(c);

                String link = linkBuilder.buildVerifyUrl(token);
                String html = templates.verificacion(saved.getNombreCompleto(), link);
                email.enviarHtml(saved.getCorreo(), "Verifica tu cuenta", html);

                return new RegistroResponse("Registro creado. Verifica tu correo.", "CLIENTE", saved.getId(), false);
            }
            case "VENDEDOR" -> {
                var v = new Vendedor();
                v.setCorreo(correo);
                v.setContrasenia(encoder.encode(req.getContrasenia()));
                v.setNombreCompleto(req.getNombreCompleto());
                v.setTelefono(req.getTelefono());
                v.setFotoPerfil(fotoGuardada);
                v.setTokenVerificacion(token);
                v.setFechaVerificacion(null);
                v.setExpiracionToken(expira);

                var saved = vendedores.save(v);

                String link = linkBuilder.buildVerifyUrl(token);
                String html = templates.verificacion(saved.getNombreCompleto(), link);
                email.enviarHtml(saved.getCorreo(), "Verifica tu cuenta", html);

                return new RegistroResponse("Registro creado. Verifica tu correo.", "VENDEDOR", saved.getId(), false);
            }
            default ->
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoUsuario inválido");
        }
    }

    public String verificar(String token) {
        var c = clientes.findByTokenVerificacion(token).orElse(null);
        if (c != null) {
            if (c.getExpiracionToken() != null && c.getExpiracionToken().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.GONE, "Token expirado");
            }
            c.setFechaVerificacion(LocalDateTime.now());
            c.setTokenVerificacion(null);
            c.setExpiracionToken(null);
            clientes.save(c);
            return "Cliente verificado";
        }

        var v = vendedores.findByTokenVerificacion(token).orElse(null);
        if (v != null) {
            if (v.getExpiracionToken() != null && v.getExpiracionToken().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.GONE, "Token expirado");
            }
            v.setFechaVerificacion(LocalDateTime.now());
            v.setTokenVerificacion(null);
            v.setExpiracionToken(null);
            vendedores.save(v);
            return "Vendedor verificado";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no válido");
    }

    public void reenviarVerificacion(String correoRaw) {
        String correo = correoRaw.toLowerCase(Locale.ROOT).trim();

        var cOpt = clientes.findAll().stream()
                .filter(x -> x.getCorreo().equalsIgnoreCase(correo)).findFirst();
        if (cOpt.isPresent()) {
            var c = cOpt.get();
            if (c.getFechaVerificacion() == null) {
                String token = UUID.randomUUID().toString();
                c.setTokenVerificacion(token);
                c.setExpiracionToken(LocalDateTime.now().plusHours(24));
                clientes.save(c);
                String link = linkBuilder.buildVerifyUrl(token);
                String html = templates.verificacion(c.getNombreCompleto(), link);
                email.enviarHtml(c.getCorreo(), "Verifica tu cuenta", html);
            }
            return;
        }

        var vOpt = vendedores.findAll().stream()
                .filter(x -> x.getCorreo().equalsIgnoreCase(correo)).findFirst();
        if (vOpt.isPresent()) {
            var v = vOpt.get();
            if (v.getFechaVerificacion() == null) {
                String token = UUID.randomUUID().toString();
                v.setTokenVerificacion(token);
                v.setExpiracionToken(LocalDateTime.now().plusHours(24));
                vendedores.save(v);
                String link = linkBuilder.buildVerifyUrl(token);
                String html = templates.verificacion(v.getNombreCompleto(), link);
                email.enviarHtml(v.getCorreo(), "Verifica tu cuenta", html);
            }
        }

    }

}
