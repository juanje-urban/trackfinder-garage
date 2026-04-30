package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingServiceResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper web para convertir líneas de detalle de servicios reservados en respuestas HTTP.
 *
 * <p>Proyecta cada servicio contratado dentro de una reserva junto con sus referencias principales y
 * el precio pagado.</p>
 */
@Component
public class EventBookingServiceWebMapper {

    public EventBookingServiceResponse toResponse(EventBookingService eventBookingService) {
        return EventBookingServiceResponse.builder()
                .id(eventBookingService.getId())
                .eventBookingId(eventBookingService.getEventBooking() != null ? eventBookingService.getEventBooking().getId() : null)
                .userId(eventBookingService.getEventBooking() != null && eventBookingService.getEventBooking().getUser() != null
                        ? eventBookingService.getEventBooking().getUser().getId()
                        : null)
                .eventId(eventBookingService.getEventBooking() != null && eventBookingService.getEventBooking().getEvent() != null
                        ? eventBookingService.getEventBooking().getEvent().getId()
                        : null)
                .eventServiceId(eventBookingService.getEventService() != null ? eventBookingService.getEventService().getId() : null)
                .priceAtPurchase(eventBookingService.getPriceAtPurchase())
                .build();
    }
}
