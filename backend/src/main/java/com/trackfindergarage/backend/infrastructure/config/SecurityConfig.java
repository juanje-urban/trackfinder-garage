package com.trackfindergarage.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de seguridad de la API.
 *
 * <p>Esta clase define tres piezas clave del backend:</p>
 * <ul>
 *     <li>La cadena de filtros de Spring Security y las reglas de acceso HTTP.</li>
 *     <li>La política CORS necesaria para permitir peticiones desde el frontend.</li>
 *     <li>El codificador de contraseñas utilizado (BCrypt).</li>
 * </ul>
 *
 * <p>Se exponen de manera pública los endpoints de consulta que sirven como escaparate del sistema,
 * como eventos, circuitos o rankings. Además, se habilita seguridad a nivel de método.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Construye la configuración principal de seguridad de la aplicación.
     *
     * <p>La cadena resultante aplica las siguientes decisiones:</p>
     * <ul>
     *     <li>Desactiva CSRF al tratarse de una API consumida por frontend separado.</li>
     *     <li>Activa CORS con la configuración declarada en {@link #corsConfigurationSource()}.</li>
     *     <li>Permite peticiones públicas a autenticación, catálogo, perfiles públicos, rankings y documentación OpenAPI.</li>
     *     <li>Exige autenticación para cualquier endpoint no incluido en la lista pública.</li>
     *     <li>Utiliza autenticación HTTP Basic como mecanismo de acceso a la API.</li>
     * </ul>
     *
     * @param http configurador de seguridad HTTP proporcionado por Spring
     * @return cadena de filtros de seguridad que se aplicará a todas las peticiones
     * @throws Exception si Spring no puede construir la configuración de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/register/organizer").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tracks", "/tracks/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/future", "/events/future/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/lap-times/user/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/event-bookings/user/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/event-bookings/event/*/visible").permitAll()
                        .requestMatchers(HttpMethod.GET, "/event-services/event/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Declara la política CORS utilizada por la API.
     *
     * <p>Permite al frontend consumir el backend desde sus orígenes locales. Es necesario porque el
     * front y el back se sirven desde puertos distintos.</p>
     *
     * @return origen de configuración CORS registrado para todas las rutas de la API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration(); // Almacena la configuración CORS.
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Proporciona el codificador de contraseñas empleado por el sistema.
     *
     * <p>Se utiliza BCrypt, que viene incluido con Spring Security.</p>
     *
     * @return codificador BCrypt para contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
