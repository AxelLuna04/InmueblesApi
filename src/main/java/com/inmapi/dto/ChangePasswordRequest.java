package com.inmapi.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
  @NotBlank private String actual;
  @NotBlank private String nueva;
}

