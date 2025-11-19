package com.inmapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RechazoRequest {
  @NotBlank
  private String motivo;
}

