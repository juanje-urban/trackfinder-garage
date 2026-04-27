package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventBookingUseCase;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ConflictException;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Implementa la lógica de reserva de eventos para usuarios estándar.
 *
 * <p>Valida disponibilidad, propiedad de la reserva, reglas de cancelación y persistencia de los
 * precios aplicados en el momento del checkout.</p>
 */
@org.springframework.stereotype.Service
@Transactional
public class EventBookingService implements EventBookingUseCase {

    private static final String EVENT_BOOKING_NOT_FOUND_WITH_ID = "Reserva de evento no encontrada con id: ";
    private static final String USER_NOT_FOUND_WITH_ID = "Usuario no encontrado con id: ";
    private static final String EVENT_NOT_FOUND_WITH_ID = "Evento no encontrado con id: ";
    private static final String USER_ID_REQUIRED = "El id de usuario es obligatorio";
    private static final String EVENT_ID_REQUIRED = "El id de evento es obligatorio";
    private static final String EVENT_BOOKING_ALREADY_EXISTS =
            "El usuario con id %d ya tiene una reserva para el evento con id %d";
    private static final String EVENT_BOOKING_REQUIRES_FUTURE_EVENT =
            "La reserva solo puede crearse para eventos futuros";
    private static final String EVENT_BOOKING_REQUIRES_EVENT_CAPACITY =
            "El número máximo de participantes del evento es obligatorio antes de aceptar reservas";
    private static final String EVENT_IS_FULL = "El evento con id %d está completo";
    private static final String EVENT_BOOKING_CANNOT_BE_DELETED_WITHIN_14_DAYS =
            "La reserva no puede eliminarse con menos de 14 días de antelación al evento";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String EVENT_SERVICE_NOT_FOUND_WITH_ID = "Servicio de evento no encontrado con id: ";
    private static final String EVENT_SERVICE_MUST_BELONG_TO_EVENT =
            "El servicio de evento con id %d no pertenece al evento con id %d";
    private static final String ONLY_STANDARD_USERS_CAN_BOOK_EVENTS =
            "Solo los usuarios estándar pueden reservar eventos";
    private static final String USER_ROLE_NAME = "USER";
    private static final String ONLY_BOOKING_OWNER_CAN_CANCEL_EVENT_BOOKING =
            "Solo el propietario de la reserva puede cancelarla";

    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final EventBookingServicePersistencePort eventBookingServicePersistencePort;
    private final EventServicePersistencePort eventServicePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final EventPersistencePort eventPersistencePort;

    public EventBookingService(EventBookingPersistencePort eventBookingPersistencePort,
                               EventBookingServicePersistencePort eventBookingServicePersistencePort,
                               EventServicePersistencePort eventServicePersistencePort,
                               UserPersistencePort userPersistencePort,
                               EventPersistencePort eventPersistencePort) {
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.eventBookingServicePersistencePort = eventBookingServicePersistencePort;
        this.eventServicePersistencePort = eventServicePersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.eventPersistencePort = eventPersistencePort;
    }

    /**
     * Crea y persiste la reserva base del evento antes de asociar servicios adicionales.
     *
     * <p>Este helper concentra las validaciones propias de la reserva: usuario y evento existentes,
     * evento futuro, unicidad por usuario, aforo disponible y fijación de los datos de compra.</p>
     *
     * @param eventBooking reserva en construcción
     * @return reserva persistida con sus datos base ya fijados
     */
    private EventBooking createEventBooking(EventBooking eventBooking) {
        validateEventBooking(eventBooking);

        Long userId = eventBooking.getUser().getId();
        Long eventId = eventBooking.getEvent().getId();

        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));
        Event event = eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        if (!event.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(EVENT_BOOKING_REQUIRES_FUTURE_EVENT);
        }

        eventBookingPersistencePort.findByUserIdAndEventId(userId, eventId)
                .ifPresent(existingEventBooking -> {
                    throw new DuplicateResourceException(EVENT_BOOKING_ALREADY_EXISTS.formatted(userId, eventId));
                });

        if (event.getMaxParticipants() == null) {
            throw new IllegalArgumentException(EVENT_BOOKING_REQUIRES_EVENT_CAPACITY);
        }

        if (eventBookingPersistencePort.countByEventId(eventId) >= event.getMaxParticipants()) {
            throw new ConflictException(EVENT_IS_FULL.formatted(eventId));
        }

        eventBooking.setUser(user);
        eventBooking.setEvent(event);
        eventBooking.setBookedAt(LocalDateTime.now());
        eventBooking.setBasePriceAtPurchase(event.getBasePrice());

        return eventBookingPersistencePort.save(eventBooking);
    }

    /**
     * Crea una reserva para el usuario autenticado y registra los servicios adicionales seleccionados.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param eventId identificador del evento reservado
     * @param eventServiceIds identificadores de servicios adicionales seleccionados
     * @param isVisible indica si la reserva será visible en el perfil público
     * @return reserva creada
     */
    @Override
    public EventBooking checkoutEventBooking(String authenticatedEmail, Long eventId, List<Long> eventServiceIds, boolean isVisible) {
        User user = loadAuthenticatedUser(authenticatedEmail);
        ensureStandardUser(user);

        EventBooking eventBooking = new EventBooking();
        eventBooking.setUser(user);
        eventBooking.setVisible(isVisible);

        Event event = new Event();
        event.setId(eventId);
        eventBooking.setEvent(event);

        // Primero se crea la reserva base; después se añaden los servicios extra seleccionados.
        EventBooking createdEventBooking = createEventBooking(eventBooking);

        // Normaliza la selección de servicios eliminando nulos y duplicados.
        List<Long> distinctEventServiceIds = eventServiceIds == null
                ? List.of()
                : eventServiceIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        distinctEventServiceIds.forEach(eventServiceId ->
                createEventBookingServiceForCheckout(createdEventBooking, eventServiceId, eventId));

        return createdEventBooking;
    }

    /**
     * Actualiza la visibilidad de una reserva que pertenece al usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador de la reserva
     * @param isVisible nuevo estado de visibilidad
     * @return reserva actualizada
     */
    @Override
    public EventBooking updateOwnEventBookingVisibility(String authenticatedEmail, Long id, boolean isVisible) {
        EventBooking eventBooking = loadOwnedBooking(authenticatedEmail, id);

        eventBooking.setVisible(isVisible);
        return eventBookingPersistencePort.save(eventBooking);
    }

    /**
     * Cancela una reserva propia si sigue cumpliendo las reglas de cancelación.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador de la reserva
     */
    @Override
    public void deleteOwnEventBooking(String authenticatedEmail, Long id) {
        deleteEventBooking(loadOwnedBooking(authenticatedEmail, id));
    }

    // Valida si la reserva aún puede cancelarse y elimina primero sus servicios asociados.
    private void deleteEventBooking(EventBooking eventBooking) {
        LocalDate deletionLimitDate = LocalDate.now().plusDays(14);
        if (eventBooking.getEvent().getEventDate().isBefore(deletionLimitDate)) {
            throw new IllegalArgumentException(EVENT_BOOKING_CANNOT_BE_DELETED_WITHIN_14_DAYS);
        }

        eventBookingServicePersistencePort.findByEventBookingId(eventBooking.getId())
                .forEach(eventBookingServicePersistencePort::delete);

        eventBookingPersistencePort.delete(eventBooking);
    }

    /**
     * Recupera las reservas del usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de reservas del usuario
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventBooking> getEventBookingsByAuthenticatedEmail(String authenticatedEmail) {
        return eventBookingPersistencePort.findByUserId(loadAuthenticatedUser(authenticatedEmail).getId());
    }

    /**
     * Recupera las reservas visibles de un usuario concreto.
     *
     * @param userId identificador del usuario
     * @return listado de reservas visibles del usuario
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventBooking> getEventBookingsByUserId(Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));

        return eventBookingPersistencePort.findByUserId(userId)
                .stream()
                .filter(EventBooking::isVisible)
                .toList();
    }

    /**
     * Recupera todas las reservas asociadas a un evento.
     *
     * @param eventId identificador del evento
     * @return listado de reservas del evento
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventBooking> getEventBookingsByEventId(Long eventId) {
        eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        return eventBookingPersistencePort.findByEventId(eventId);
    }

    // Valida los datos mínimos de entrada para crear una reserva.
    private void validateEventBooking(EventBooking eventBooking) {
        if (eventBooking.getUser() == null || eventBooking.getUser().getId() == null) {
            throw new IllegalArgumentException(USER_ID_REQUIRED);
        }
        if (eventBooking.getEvent() == null || eventBooking.getEvent().getId() == null) {
            throw new IllegalArgumentException(EVENT_ID_REQUIRED);
        }
    }

    // Carga y normaliza el usuario autenticado a partir de su correo.
    private User loadAuthenticatedUser(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = normalizeEmail(authenticatedEmail);
        return userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));
    }

    // Restringe las reservas a usuarios con rol estándar.
    private void ensureStandardUser(User user) {
        if (user.getRole() == null
                || user.getRole().getRoleName() == null
                || !USER_ROLE_NAME.equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new AccessDeniedException(ONLY_STANDARD_USERS_CAN_BOOK_EVENTS);
        }
    }

    // Carga la reserva y comprueba que pertenece al usuario autenticado.
    private EventBooking loadOwnedBooking(String authenticatedEmail, Long bookingId) {
        User currentUser = loadAuthenticatedUser(authenticatedEmail);
        EventBooking eventBooking = eventBookingPersistencePort.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_BOOKING_NOT_FOUND_WITH_ID + bookingId));

        if (eventBooking.getUser() == null
                || eventBooking.getUser().getEmail() == null
                || !normalizeEmail(currentUser.getEmail()).equals(normalizeEmail(eventBooking.getUser().getEmail()))) {
            throw new AccessDeniedException(ONLY_BOOKING_OWNER_CAN_CANCEL_EVENT_BOOKING);
        }

        return eventBooking;
    }

    // Normaliza el correo para comparaciones y búsquedas.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Crea la asociación entre la reserva y cada servicio extra seleccionado.
    private void createEventBookingServiceForCheckout(EventBooking eventBooking, Long eventServiceId, Long eventId) {
        EventService eventService = eventServicePersistencePort.findById(eventServiceId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_SERVICE_NOT_FOUND_WITH_ID + eventServiceId));

        if (eventService.getEvent() == null || !eventId.equals(eventService.getEvent().getId())) {
            throw new IllegalArgumentException(EVENT_SERVICE_MUST_BELONG_TO_EVENT.formatted(eventServiceId, eventId));
        }

        com.trackfindergarage.backend.domain.model.EventBookingService eventBookingService =
                new com.trackfindergarage.backend.domain.model.EventBookingService();
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(eventService.getPrice());

        eventBookingServicePersistencePort.save(eventBookingService);
    }
}
