package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Organizer;

import java.util.List;

public interface OrganizerUseCase {

    Organizer createOrganizer(Organizer organizer, String rawPassword);

    Organizer getCurrentOrganizer(String authenticatedEmail);

    Organizer updateCurrentOrganizerProfile(String authenticatedEmail,
                                            UpdateCurrentOrganizerProfileCommand command);

    Organizer updateOrganizer(Long id, Organizer organizer);

    void deleteOrganizer(Long id);

    List<Organizer> getAllOrganizers();

    Organizer getOrganizerById(Long id);

    Organizer enableOrganizer(Long id);

    Organizer disableOrganizer(Long id);
}
