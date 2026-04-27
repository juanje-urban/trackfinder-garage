package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerCatalogServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerWorkspaceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpsertOrganizerEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWorkspaceWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints HTTP del espacio de trabajo del organizador.
 */
@RestController
@RequestMapping("/organizer-workspace")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerWorkspaceController extends AbstractWebController {

    private final OrganizerWorkspaceUseCase organizerWorkspaceUseCase;
    private final OrganizerWorkspaceWebMapper organizerWorkspaceWebMapper;

    public OrganizerWorkspaceController(OrganizerWorkspaceUseCase organizerWorkspaceUseCase,
                                        OrganizerWorkspaceWebMapper organizerWorkspaceWebMapper) {
        this.organizerWorkspaceUseCase = organizerWorkspaceUseCase;
        this.organizerWorkspaceWebMapper = organizerWorkspaceWebMapper;
    }

    /**
     * Recupera la vista completa del workspace del organizador.
     *
     * @param authentication autenticación del usuario actual
     * @return respuesta con el estado del workspace
     */
    @GetMapping
    public OrganizerWorkspaceResponse getWorkspace(Authentication authentication) {
        return organizerWorkspaceWebMapper.toResponse(
                organizerWorkspaceUseCase.getWorkspace(authenticatedEmail(authentication))
        );
    }

    /**
     * Añade un servicio del catálogo al organizador.
     *
     * @param request petición con el servicio a añadir
     * @param authentication autenticación del usuario actual
     * @return workspace actualizado
     */
    @PostMapping("/services")
    public OrganizerWorkspaceResponse addOrganizerService(@Valid @RequestBody CreateOrganizerCatalogServiceRequest request,
                                                          Authentication authentication) {
        return organizerWorkspaceWebMapper.toResponse(
                organizerWorkspaceUseCase.addOrganizerService(
                        authenticatedEmail(authentication),
                        request.getServiceId()
                )
        );
    }

    /**
     * Elimina un servicio propio del organizador.
     *
     * @param organizerServiceId identificador de la asignación a eliminar
     * @param authentication autenticación del usuario actual
     * @return workspace actualizado
     */
    @DeleteMapping("/services/{organizerServiceId}")
    public OrganizerWorkspaceResponse removeOrganizerService(@PathVariable Long organizerServiceId,
                                                             Authentication authentication) {
        return organizerWorkspaceWebMapper.toResponse(
                organizerWorkspaceUseCase.removeOrganizerService(
                        authenticatedEmail(authentication),
                        organizerServiceId
                )
        );
    }

    /**
     * Crea un evento nuevo desde el workspace del organizador.
     *
     * @param request petición con los datos del evento
     * @param authentication autenticación del usuario actual
     * @return workspace actualizado
     */
    @PostMapping("/events")
    public OrganizerWorkspaceResponse createEvent(@Valid @RequestBody UpsertOrganizerEventRequest request,
                                                  Authentication authentication) {
        return organizerWorkspaceWebMapper.toResponse(
                organizerWorkspaceUseCase.createEvent(
                        authenticatedEmail(authentication),
                        organizerWorkspaceWebMapper.toDraft(request)
                )
        );
    }

    /**
     * Actualiza un evento existente del organizador.
     *
     * @param eventId identificador del evento a modificar
     * @param request petición con los nuevos datos del evento
     * @param authentication autenticación del usuario actual
     * @return workspace actualizado
     */
    @PutMapping("/events/{eventId}")
    public OrganizerWorkspaceResponse updateEvent(@PathVariable Long eventId,
                                                  @Valid @RequestBody UpsertOrganizerEventRequest request,
                                                  Authentication authentication) {
        return organizerWorkspaceWebMapper.toResponse(
                organizerWorkspaceUseCase.updateEvent(
                        authenticatedEmail(authentication),
                        eventId,
                        organizerWorkspaceWebMapper.toDraft(request)
                )
        );
    }

    /**
     * Elimina un evento del organizador.
     *
     * @param eventId identificador del evento a eliminar
     * @param authentication autenticación del usuario actual
     * @return workspace actualizado
     */
    @DeleteMapping("/events/{eventId}")
    public OrganizerWorkspaceResponse deleteEvent(@PathVariable Long eventId,
                                                  Authentication authentication) {
        return organizerWorkspaceWebMapper.toResponse(
                organizerWorkspaceUseCase.deleteEvent(
                        authenticatedEmail(authentication),
                        eventId
                )
        );
    }
}
