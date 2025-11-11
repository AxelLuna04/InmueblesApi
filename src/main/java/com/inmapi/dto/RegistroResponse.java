package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RegistroResponse {
  private String mensaje;
  private String tipoUsuario;
  private Integer id;
  private boolean verificado; 
}

