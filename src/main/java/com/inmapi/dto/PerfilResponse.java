package com.inmapi.dto;

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
  private Integer idOcupacion;
  private String telefono;
  private Integer idFotoPerfil;
  private String rutaFoto;
}

