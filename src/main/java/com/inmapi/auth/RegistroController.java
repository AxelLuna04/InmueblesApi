package com.inmapi.auth;

import com.inmapi.dto.RegistroRequest;
import com.inmapi.dto.RegistroResponse;
import com.inmapi.modelo.FotoPerfil;
import com.inmapi.service.CuentaService;
import com.inmapi.service.FotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registro;
    private final FotoService fotos;
    private final CuentaService cuenta;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistroResponse> register(
            @RequestPart("datos") @Valid RegistroRequest datos,
            @RequestPart("foto") MultipartFile foto) {

        FotoPerfil fp = fotos.guardarFotoPerfil(foto);
        var res = registro.registrar(datos, fp);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        String msg = registro.verificar(token);
        return ResponseEntity.ok().body(java.util.Map.of("mensaje", msg));
    }
    
    
    @GetMapping("/confirm-email-change")
    public ResponseEntity<?> confirmEmailChange(@RequestParam("token") String token) {
        String msg = cuenta.confirmarCambioCorreo(token);
        return ResponseEntity.ok().body(java.util.Map.of("mensaje", msg));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resend(@RequestParam("correo") String correo) {
        registro.reenviarVerificacion(correo);
        return ResponseEntity.ok().body(java.util.Map.of("mensaje", "Si el correo existe y no está verificado, se envió un nuevo enlace."));
    }
}

