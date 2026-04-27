package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa la reserva que un usuario realiza de un evento.
 *
 * <p>Guarda la fecha de compra, el precio base pagado en ese momento y si la reserva puede mostrarse
 * públicamente en el perfil del usuario.</p>
 */
@Entity
@Table(
        name = "event_bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_bookings_user_event",
                        columnNames = {"id_user", "id_event"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EventBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_event", nullable = false)
    private Event event;

    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt;

    @Column(name = "base_price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePriceAtPurchase;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;
}
