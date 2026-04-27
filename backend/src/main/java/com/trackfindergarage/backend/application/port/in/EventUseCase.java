package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Event;

import java.util.List;

/**
 * Define los casos de uso relacionados con la gestión de eventos.
 */
public interface EventUseCase {

    /**
     * Crea un nuevo evento.
     *
     * @param event datos del evento
     * @return evento persistido
     */
    Event createEvent(Event event);

    /**
     * Actualiza un evento existente.
     *
     * @param id identificador del evento a modificar
     * @param event nuevos datos del evento
     * @return evento actualizado
     */
    Event updateEvent(Long id, Event event);

    /**
     * Elimina un evento existente.
     *
     * @param id identificador del evento a eliminar
     */
    void deleteEvent(Long id);

    /**
     * Recupera los eventos futuros visibles para el catálogo público.
     *
     * @return listado de eventos futuros
     */
    List<Event> getFutureEvents();

    /**
     * Recupera un evento por su identificador.
     *
     * @param id identificador del evento
     * @return evento encontrado
     */
    Event getEventById(Long id);

    /**
     * Calcula las plazas libres que quedan en un evento.
     *
     * @param eventId identificador del evento
     * @return plazas restantes
     */
    int getRemainingCapacity(Long eventId);

}
