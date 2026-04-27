package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Representa un servicio adicional contratado dentro de una reserva de evento.
 *
 * <p>Conserva el precio del servicio en el momento de la compra para no depender de cambios futuros
 * en la tarifa del evento (que puede variar según las reglas de negocio).</p>
 */
@Entity
@Table(
        name = "event_booking_services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_booking_services_booking_service",
                        columnNames = {"id_event_booking", "id_event_service"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EventBookingService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_event_booking", nullable = false)
    private EventBooking eventBooking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_event_service", nullable = false)
    private EventService eventService;

    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
}
