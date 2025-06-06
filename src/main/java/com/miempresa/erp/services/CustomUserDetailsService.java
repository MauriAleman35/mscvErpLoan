package com.miempresa.erp.services;

import com.miempresa.erp.domain.User;
import com.miempresa.erp.repository.UserRepository;
import com.miempresa.erp.security.AuthoritiesConstants;
import com.miempresa.erp.security.CustomUserDetails;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);

        return userRepository
            .findByEmailIgnoreCase(login)
            .map(user -> createCustomUser(user))
            .orElseThrow(() -> new UsernameNotFoundException("User with email " + login + " was not found in the database"));
    }

    private CustomUserDetails createCustomUser(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Asignar autoridad basada en user_type
        if ("prestamista".equalsIgnoreCase(user.getUserType())) {
            authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
        } else {
            authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        }

        // Ya NO intentamos cargar roles de la base de datos

        return new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getUserType(),
            authorities,
            true // Asumimos que todos los usuarios están activados
        );
    }
}
