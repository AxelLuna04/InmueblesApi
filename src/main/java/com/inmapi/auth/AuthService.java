package com.inmapi.auth;

import com.inmapi.dto.LoginResponse;
import com.inmapi.modelo.UsuarioLoginView;
import com.inmapi.repository.UsuarioLoginRepository;
import com.inmapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioLoginRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public LoginResponse login(String correo, String contraseniaPlano) {
    String key = correo.toLowerCase(Locale.ROOT).trim();
    UsuarioLoginView u = users.findByCorreo(key)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

    if (!encoder.matches(contraseniaPlano, u.getContrasenia())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }
    
    if (u.getFechaVerificacion() == null) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no verificada. Revisa tu email.");
    }

    String access = jwt.newAccess(u.getCorreo(), u.getRol());
    String refresh = jwt.newRefresh(u.getCorreo());
    return new LoginResponse(access, refresh, u.getRol());
  }

  public LoginResponse refresh(String refreshToken) {
    var authOpt = jwt.toAuth(refreshToken);
    if (authOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh inválido");
    String correo = (String) authOpt.get().getPrincipal();
    String access = jwt.newAccess(correo, "USER");
    return new LoginResponse(access, refreshToken, "USER");
  }
}
