package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Define un rol funcional de la aplicación.
 *
 * <p>Los roles se reutilizan desde {@link User} para expresar permisos de alto nivel como usuario final, organizador
 * o administrador.</p>
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_role", columnNames = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Role {

    /**
     * Identificador interno del rol.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Nombre funcional del rol, por ejemplo {@code USER}, {@code ORGANIZER} o {@code ADMIN}.
     */
    @Column(name = "role", length = 100, nullable = false)
    private String roleName;
}
