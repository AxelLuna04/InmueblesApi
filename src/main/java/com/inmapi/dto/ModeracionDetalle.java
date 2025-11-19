package com.inmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data @AllArgsConstructor
public class ModeracionDetalle {
  private Integer id;
  private String estado;
  private String motivoRechazo;
  private String titulo;
  private String descripcion;
  private Double precio;
  private Integer habitaciones;
  private Integer banos;
  private Integer excusados;
  private String tipoInmueble;
  private String vendedorNombre;
  private String vendedorCorreo;
  private String direccion;
  private Double lat;
  private Double lng;
  private java.time.LocalDateTime creadoEn;
  private List<String> fotos;
  private List<String> caracteristicas;
}

