package com.trackfindergarage.backend.application.port.in;

import java.math.BigDecimal;

/**
 * Representa la selección de un servicio adicional dentro del formulario de evento.
 */
public record OrganizerEventServiceDraft(
        Long trackServiceId,
        Long organizerServiceId,
        BigDecimal price
) {
}
