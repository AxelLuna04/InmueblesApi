// UpdatePerfilClienteRequest.java  -> PATCH de cliente
package com.inmapi.dto;

import lombok.Data;

@Data
public class UpdatePerfilClienteRequest {
  private String nombreCompleto;
  private Double presupuesto;
  private String ubicacionInteres;
  private String numeroMiembrosFamilia;
  private Integer idOcupacion;
}

