package com.inmapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${security.jwt.secret}") private String secret;
  @Value("${security.jwt.access.minutes}") private long accessMinutes;
  @Value("${security.jwt.refresh.days}") private long refreshDays;

  private Key key() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }

  public String generate(String subject, Map<String,Object> claims, Duration ttl) {
    var now = Instant.now();
    return Jwts.builder()
        .setSubject(subject)
        .addClaims(claims)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(ttl)))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String newAccess(String correo, String rol) {
    return generate(correo, Map.of("rol", rol), Duration.ofMinutes(accessMinutes));
  }

  public String newRefresh(String correo) {
    return generate(correo, Map.of("type", "refresh"), Duration.ofDays(refreshDays));
  }

  public Optional<Authentication> toAuth(String token) {
    try {
      var claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
      String email = claims.getSubject();
      String rol = String.valueOf(claims.getOrDefault("rol", "USER"));
      var auths = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
      return Optional.of(new UsernamePasswordAuthenticationToken(email, null, auths));
    } catch (JwtException e) {
      return Optional.empty();
    }
  }
}

