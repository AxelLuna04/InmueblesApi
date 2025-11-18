package com.inmapi.dto;

import lombok.*;
import java.util.List;

@Data @AllArgsConstructor
public class PublicacionDetalle {
  private Integer id;
  private String titulo;
  private String descripcion;
  private Double precio;
  private Integer habitaciones;
  private Integer banosCompletos;
  private Integer excusados;

  private String tipoInmueble;
  private DireccionDTO direccion;
  private List<String> fotos;
  private List<String> caracteristicas;
  private String vendedorNombre;
}
