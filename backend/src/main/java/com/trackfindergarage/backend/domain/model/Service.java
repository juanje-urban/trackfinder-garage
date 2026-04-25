package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un servicio adicional que la plataforma puede ofrecer en catálogo.
 *
 * <p>Un servicio puede estar disponible para circuitos, para organizadores o para ambos, y además puede
 * activarse o desactivarse sin eliminarlo del sistema.</p>
 */
@Entity
@Table(
        name = "services",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_services_name", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Service {

    /**
     * Id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Nombre del servicio.
     */
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    /**
     * Descripción.
     */
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * Indica si el servicio puede ser ofrecido por un circuito.
     */
    @Column(name = "allowed_for_track")
    private Boolean allowedForTrack;

    /**
     * Indica si el servicio puede ser ofrecido por un organizador.
     */
    @Column(name = "allowed_for_organizer")
    private Boolean allowedForOrganizer;

    /**
     * Indica si el servicio está activo.
     */
    @Column(name = "enabled")
    private Boolean enabled;
}
