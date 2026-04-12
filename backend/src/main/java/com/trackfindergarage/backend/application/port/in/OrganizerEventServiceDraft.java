package com.trackfindergarage.backend.application.port.in;

import java.math.BigDecimal;

public record OrganizerEventServiceDraft(
        Long trackServiceId,
        Long organizerServiceId,
        BigDecimal price
) {
}
