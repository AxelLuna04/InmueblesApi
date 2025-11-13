package com.inmapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {
  @NotBlank
  private String contraseniaActual;
}

