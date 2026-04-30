package com.trackfindergarage.backend.infrastructure.config;

import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Adaptador entre el modelo de usuario de la aplicación y Spring Security.
 *
 * <p>Localiza usuarios por correo electrónico y construye el {@link UserDetails} que Spring usa
 * para autenticar peticiones HTTP Basic.</p>
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserPersistencePort userPersistencePort;

    public AppUserDetailsService(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedEmail = username.trim().toLowerCase(Locale.ROOT);

        User user = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        String roleName = user.getRole() != null && user.getRole().getRoleName() != null
                ? user.getRole().getRoleName().trim().toUpperCase(Locale.ROOT)
                : "USER";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleName)))
                .build();
    }
}
