package com.inmapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CrearPublicacionRequest {
    
  @NotBlank @Size(max = 50)
  private String titulo;
  
  @NotBlank
  @Pattern(regexp = "RENTA|VENTA")
  private String tipoOperacion;
  
  @NotBlank @Size(max = 200)
  private String descripcion;
  
  @NotNull
  private Double precio;
  
  private Integer numeroHabitaciones;
  private Integer numeroBanosCompletos;
  private Integer numeroExcusados;

  @NotNull
  private Integer idTipoInmueble;

  @NotNull
  private DireccionDTO direccion;

  private List<Integer> caracteristicasIds;

  @NotNull
  private Integer indicePortada;
}

