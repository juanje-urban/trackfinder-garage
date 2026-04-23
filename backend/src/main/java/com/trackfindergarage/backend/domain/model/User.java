package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa la cuenta base de cualquier usuario.
 *
 * <p>Contiene la información común necesaria para autenticación, perfil y permisos. Un usuario puede actuar como
 * usuario final, administrador u organizador, según el {@link Role} asociado.</p>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "display_name"),
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * Identificador interno del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    /**
     * Alias público mostrado en la aplicación.
     */
    @Column(name = "display_name", length = 255)
    private String displayName;

    /**
     * Hash de la contraseña generado por la capa de seguridad.
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /**
     * Email utilizado como credencial de acceso.
     */
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Fecha y hora de alta de la cuenta.
     */
    @Column(name = "created")
    private LocalDateTime created;

    /**
     * Indica si la cuenta está habilitada para operar en la plataforma.
     */
    @Column(name = "enabled")
    private Boolean enabled;

    /**
     * Nombre real del usuario.
     */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * Apellidos del usuario.
     */
    @Column(name = "surname", length = 255)
    private String surname;

    /**
     * Dirección de contacto del usuario.
     */
    @Column(name = "address", length = 255)
    private String address;

    /**
     * Teléfono de contacto del usuario.
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Rol funcional que determina permisos y comportamiento dentro del sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role")
    private Role role;
}
