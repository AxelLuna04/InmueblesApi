package com.inmapi.dto;

import lombok.Data;

@Data
public class PublicacionFiltro {
  private Integer idTipoInmueble;
  private Double precioMin;
  private Double precioMax;
  private Integer habMin;
  private Integer banosMin;
  private Integer excusadosMin;
  private String q;
  private String ubicacion;
  private String caracteristicas;
  private String tipoOperacion;
}
