package com.miempresa.erp.graphql.auth;

import com.miempresa.erp.security.CustomUserDetails;
import com.miempresa.erp.services.JwtService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;

@Controller
public class AuthResolver {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResolver(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @MutationMapping
    public Map<String, Object> login(@Argument String username, @Argument String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            String jwt = jwtService.generateToken(authentication);

            Map<String, Object> result = new HashMap<>();
            result.put("token", jwt);

            List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

            result.put("roles", roles);

            // Intentamos obtener userType, pero no fallamos si no está disponible
            try {
                Object principal = authentication.getPrincipal();
                if (principal instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) principal;
                    result.put("userType", userDetails.getUserType());
                }
            } catch (Exception e) {
                // No hacer nada, simplemente no incluimos userType
            }

            return result;
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }
}
