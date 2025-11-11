package com.inmapi.service;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

  public String verificacion(String nombre, String link) {
    return """
      <div style="font-family: Arial, sans-serif; max-width: 520px;">
        <h2>¡Bienvenido(a)!</h2>
        <p>Hola %s,</p>
        <p>Para activar tu cuenta, por favor haz clic en el siguiente botón:</p>
        <p>
          <a href="%s" style="display:inline-block;padding:10px 16px;background:#2563eb;color:#fff;
             text-decoration:none;border-radius:6px;">Verificar mi cuenta</a>
        </p>
        <p>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
        <p><a href="%s">%s</a></p>
        <p>Este enlace expira en 24 horas.</p>
      </div>
      """.formatted(nombre == null ? "" : nombre, link, link, link);
  }
}

