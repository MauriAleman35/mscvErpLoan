package com.miempresa.erp.config;

import com.miempresa.erp.security.AuthoritiesConstants;
import com.miempresa.erp.security.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final CorsFilter corsFilter;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    public SecurityConfiguration(CorsFilter corsFilter, JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.corsFilter = corsFilter;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz ->
                authz
                    .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/authenticate"))
                    .permitAll()
                    .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/authenticate"))
                    .permitAll()
                    .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/register"))
                    .permitAll()
                    .requestMatchers(mvc.pattern("/api/admin/**"))
                    .hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(mvc.pattern("/api/**"))
                    .authenticated()
                    .requestMatchers(mvc.pattern("/v3/api-docs/**"))
                    .hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(mvc.pattern("/management/health"))
                    .permitAll()
                    .requestMatchers(mvc.pattern("/management/health/**"))
                    .permitAll()
                    .requestMatchers(mvc.pattern("/management/info"))
                    .permitAll()
                    .requestMatchers(mvc.pattern("/management/prometheus"))
                    .permitAll()
                    .requestMatchers(mvc.pattern("/management/**"))
                    .hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(mvc.pattern("/graphql"))
                    .permitAll()
                    .requestMatchers(mvc.pattern("/graphiql/**"))
                    .permitAll()
            );

        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }
}
