package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventService;

import java.util.List;

/**
 * Define los casos de uso relacionados con los servicios adicionales de un evento.
 */
public interface EventServiceUseCase {

    /**
     * Asocia un nuevo servicio a un evento.
     *
     * @param eventService datos de la asociación
     * @return servicio de evento persistido
     */
    EventService createEventService(EventService eventService);

    /**
     * Actualiza un servicio ya asociado a un evento.
     *
     * @param id identificador de la asociación a modificar
     * @param eventService nuevos datos de la asociación
     * @return servicio de evento actualizado
     */
    EventService updateEventService(Long id, EventService eventService);

    /**
     * Elimina un servicio asociado a un evento.
     *
     * @param id identificador de la asociación a eliminar
     */
    void deleteEventService(Long id);

    /**
     * Recupera los servicios configurados para un evento concreto.
     *
     * @param eventId identificador del evento
     * @return listado de servicios del evento
     */
    List<EventService> getEventServicesByEventId(Long eventId);
}
