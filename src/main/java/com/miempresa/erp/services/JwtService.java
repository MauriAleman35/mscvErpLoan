package com.miempresa.erp.services;

import com.miempresa.erp.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    private String secretKey;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:86400}")
    private long tokenValidityInSeconds;

    // Genera la clave secreta a partir de la cadena
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();

        // Extraer roles/authorities
        List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        claims.put("roles", roles);

        // Si es un UserDetails personalizado, extraemos información adicional
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            claims.put("user_type", userDetails.getUserType());
            claims.put("user_id", userDetails.getId());
        }

        long now = (new Date()).getTime();
        Date validity = new Date(now + tokenValidityInSeconds * 1000);

        return Jwts.builder()
            .setClaims(claims) // Cambiado de .claims() a .setClaims()
            .setSubject(authentication.getName())
            .setIssuedAt(new Date())
            .setExpiration(validity)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Cambiado a esta forma
            .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public Claims extractAllClaims(String token) {
        // Esta versión funciona con versiones antiguas de jjwt
        return Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token).getBody();
    }
}
