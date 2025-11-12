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
    
    public String verificacionTxt(String nombre, String link) {
        return """
          Hola %s,

          Para activar tu cuenta, por favor copia y pega este enlace en tu navegador:
          %s
          
          Este enlace expira en 24 horas.
          """.formatted(nombre == null ? "" : nombre, link);
    }

    public String avisoCambioContrasenia(String nombre) {
        return """
    <p>Hola %s,</p>
    <p>Tu contraseña ha sido actualizada. Si no fuiste tú, por favor cambia tu contraseña de inmediato.</p>
  """.formatted(nombre == null ? "" : nombre);
    }

    public String avisoCambioContraseniaTxt(String nombre) {
        return """
    Hola %s,

    Tu contraseña ha sido actualizada. Si no fuiste tú, cambia tu contraseña de inmediato.
  """.formatted(nombre == null ? "" : nombre);
    }

    public String confirmarCambioCorreo(String nombre, String link, String nuevoCorreo) {
        return """
    <p>Hola %s,</p>
    <p>Solicitaste cambiar tu correo a <b>%s</b>. Para confirmar, haz clic aquí:</p>
    <p><a href="%s">Confirmar cambio de correo</a></p>
    <p>Si tú no hiciste esta solicitud, ignora este mensaje.</p>
  """.formatted(nombre == null ? "" : nombre, nuevoCorreo, link);
    }

    public String confirmarCambioCorreoTxt(String nombre, String link, String nuevoCorreo) {
        return """
    Hola %s,

    Solicitaste cambiar tu correo a %s.
    Para confirmar, abre este enlace:
    %s

    Si no fuiste tú, ignora este mensaje.
  """.formatted(nombre == null ? "" : nombre, nuevoCorreo, link);
    }

}

