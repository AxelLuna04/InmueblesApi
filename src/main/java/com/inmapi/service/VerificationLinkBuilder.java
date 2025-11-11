package com.inmapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerificationLinkBuilder {

  @Value("${app.public.url}")
  private String publicApiUrl;

  public String buildVerifyUrl(String token) {
    String base = publicApiUrl.endsWith("/") ? publicApiUrl.substring(0, publicApiUrl.length()-1) : publicApiUrl;
    return base + "/v1/auth/verify?token=" + token;
  }
}
