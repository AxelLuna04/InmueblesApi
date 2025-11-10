package com.inmapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173") //Este puerto debemos cambiarlo dependiendo del front
        .allowedMethods("GET","POST","PUT","DELETE","PATCH")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}

