package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CrearPublicacionResponse {
  private Integer idPublicacion;
  private String estado;
  private String mensaje;
}
