package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Representa un servicio adicional ofertado dentro de un evento.
 *
 * <p>Puede apuntar a un servicio propio del circuito o a uno del organizador, junto con el precio
 * concreto con el que se ofrece en ese evento.</p>
 */
@Entity
@Table(
        name = "event_services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_services_event_track_service",
                        columnNames = {"id_event", "id_track_service"}
                ),
                @UniqueConstraint(
                        name = "uk_event_services_event_organizer_service",
                        columnNames = {"id_event", "id_organizer_service"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EventService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_event", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_track_service")
    private TrackService trackService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizer_service")
    private OrganizerService organizerService;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
