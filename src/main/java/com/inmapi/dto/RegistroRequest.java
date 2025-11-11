package com.inmapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroRequest {
  @NotBlank
  private String tipoUsuario;

  @Email @NotBlank
  private String correo;

  @NotBlank @Size(min = 8, max = 64)
  private String contrasenia;

  @NotBlank
  private String nombreCompleto;

  private java.time.LocalDate fechaNacimiento;

  private String telefono;
}

