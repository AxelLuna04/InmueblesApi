package com.inmapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class PerfilResponse {
  private String tipoUsuario;
  private Integer id;
  private String correo;
  private String nombreCompleto;
  private Double presupuesto;
  private String ubicacionInteres;
  private String numeroMiembrosFamilia;
  @JsonFormat(pattern = "yyyy-MM-DD")
  private LocalDate fechaNacimiento;
  private Integer idOcupacion;
  private String telefono;
  private Integer idFotoPerfil;
  private String rutaFoto;
}

