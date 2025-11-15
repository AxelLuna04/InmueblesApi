package com.inmapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DireccionDTO {
  @NotBlank private String formattedAddress;
  @NotBlank private String line1;
  @NotBlank private String sublocality;
  @NotBlank private String locality;
  private String adminArea2;
  @NotBlank private String adminArea1;
  @NotBlank private String postalCode;
  @NotBlank private String countryCode;
  @NotNull  private Double lat;
  @NotNull  private Double lng;
  private String provider;
  private String providerPlaceId;
}

