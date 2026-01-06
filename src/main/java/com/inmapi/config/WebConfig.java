package com.inmapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String base = Paths.get("uploads").toAbsolutePath().toUri().toString();
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations(base)
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
  }
}

