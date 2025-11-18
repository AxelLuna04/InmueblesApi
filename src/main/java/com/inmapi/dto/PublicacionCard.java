package com.inmapi.dto;

import lombok.*;

@Data @AllArgsConstructor
public class PublicacionCard {
  private Integer id;
  private String titulo;
  private Double precio;
  private String direccionCorta;
  private Integer habitaciones;
  private Integer banos;
  private Integer excusados;
  private String portada;
  private String tipoInmueble;
}

