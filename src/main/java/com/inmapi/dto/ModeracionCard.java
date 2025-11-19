package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ModeracionCard {
  private Integer id;
  private String titulo;
  private Double precio;
  private String tipoInmueble;
  private String direccionCorta;
  private String portada;
  private String estado;
  private java.time.LocalDateTime creadoEn;
}

