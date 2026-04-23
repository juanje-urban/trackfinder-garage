package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWebMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizerControllerTest {

    private final OrganizerUseCase organizerUseCase = mock(OrganizerUseCase.class);
    private final OrganizerWebMapper organizerWebMapper = new OrganizerWebMapper();
    private final OrganizerController organizerController = new OrganizerController(organizerUseCase, organizerWebMapper);

    @Test
    void deleteOrganizerDelegatesToUseCase() {
        organizerController.deleteOrganizer(5L);

        verify(organizerUseCase).deleteOrganizer(5L);
    }

    @Test
    void getAllOrganizersMapsResponses() {
        when(organizerUseCase.getAllOrganizers())
                .thenReturn(List.of(organizer(5L, "tracklimits"), organizer(6L, "paddock")));

        List<OrganizerResponse> responses = organizerController.getAllOrganizers();

        assertEquals(2, responses.size());
        assertEquals("tracklimits", responses.get(0).getDisplayName());
    }

    @Test
    void enableOrganizerMapsResponse() {
        when(organizerUseCase.enableOrganizer(5L)).thenReturn(organizer(5L, "tracklimits"));

        OrganizerResponse response = organizerController.enableOrganizer(5L);

        assertEquals(5L, response.getIdUser());
        assertEquals("ORGANIZER", response.getRoleName());
    }

    private Organizer organizer(Long id, String displayName) {
        Role role = new Role();
        role.setId(2L);
        role.setRoleName("ORGANIZER");

        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName + "@example.com");
        user.setEnabled(true);
        user.setRole(role);

        Organizer organizer = new Organizer();
        organizer.setIdUser(id);
        organizer.setUser(user);
        organizer.setLegalName("Track Events");
        organizer.setCif("B12345678");
        organizer.setEnabled(true);
        return organizer;
    }
}
