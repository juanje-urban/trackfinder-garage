package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modela la relación de un servicio de catálogo a un circuito concreto.
 *
 * <p>Esta relación define que servicios propios de pista puede ofrecer un circuito.</p>
 */
@Entity
@Table(
        name = "track_services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_track_services_track_service",
                        columnNames = {"id_track", "id_service"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TrackService {

    /**
     * Identificador interno de la relación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Circuito al que se asocia el servicio.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_track", nullable = false)
    private Track track;

    /**
     * Servicio de catalogo habilitado para la pista.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false)
    private Service service;
}
