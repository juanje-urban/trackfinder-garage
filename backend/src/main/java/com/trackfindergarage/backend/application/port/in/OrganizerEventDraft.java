package com.trackfindergarage.backend.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrganizerEventDraft(
        Long trackId,
        LocalDate eventDate,
        BigDecimal basePrice,
        Integer maxParticipants,
        String description,
        List<OrganizerEventServiceDraft> services
) {
}
