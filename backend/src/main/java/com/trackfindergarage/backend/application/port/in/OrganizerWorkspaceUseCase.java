package com.trackfindergarage.backend.application.port.in;

public interface OrganizerWorkspaceUseCase {

    OrganizerWorkspaceSnapshot getWorkspace(String authenticatedEmail);

    OrganizerWorkspaceSnapshot addOrganizerService(String authenticatedEmail, Long serviceId);

    OrganizerWorkspaceSnapshot removeOrganizerService(String authenticatedEmail, Long organizerServiceId);

    OrganizerWorkspaceSnapshot createEvent(String authenticatedEmail, OrganizerEventDraft draft);

    OrganizerWorkspaceSnapshot updateEvent(String authenticatedEmail, Long eventId, OrganizerEventDraft draft);

    OrganizerWorkspaceSnapshot deleteEvent(String authenticatedEmail, Long eventId);
}
