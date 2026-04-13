package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.application.port.in.UpdateCurrentOrganizerProfileCommand;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateCurrentOrganizerProfileRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrentOrganizerControllerTest {

    private final OrganizerUseCase organizerUseCase = mock(OrganizerUseCase.class);
    private final OrganizerWebMapper organizerWebMapper = new OrganizerWebMapper();
    private final CurrentOrganizerController currentOrganizerController =
            new CurrentOrganizerController(organizerUseCase, organizerWebMapper);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void getCurrentOrganizerReturnsMappedResponse() {
        when(authentication.getName()).thenReturn("organizer@example.com");
        when(organizerUseCase.getCurrentOrganizer("organizer@example.com"))
                .thenReturn(organizerWithId(5L, "organizer"));

        OrganizerResponse response = currentOrganizerController.getCurrentOrganizer(authentication);

        assertEquals(5L, response.getIdUser());
        assertEquals("organizer", response.getDisplayName());
        assertEquals("ORGANIZER", response.getRoleName());
    }

    @Test
    void updateCurrentOrganizerDelegatesToUseCase() {
        when(authentication.getName()).thenReturn("organizer@example.com");

        UpdateCurrentOrganizerProfileRequest request = new UpdateCurrentOrganizerProfileRequest();
        request.setName("Name");
        request.setSurname("Surname");
        request.setEmail("organizer@example.com");
        request.setAddress("Address");
        request.setPhone("123456789");
        request.setLegalName("Track Events");
        request.setCif("B12345678");
        request.setPassword("secret");

        when(organizerUseCase.updateCurrentOrganizerProfile(
                eq("organizer@example.com"),
                any(UpdateCurrentOrganizerProfileCommand.class)
        )).thenReturn(organizerWithId(5L, "organizer"));

        OrganizerResponse response = currentOrganizerController.updateCurrentOrganizer(authentication, request);

        assertEquals(5L, response.getIdUser());
        verify(organizerUseCase).updateCurrentOrganizerProfile(
                eq("organizer@example.com"),
                any(UpdateCurrentOrganizerProfileCommand.class)
        );
    }

    private Organizer organizerWithId(Long id, String displayName) {
        Role role = new Role();
        role.setId(2L);
        role.setRoleName("ORGANIZER");

        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName + "@example.com");
        user.setName("Name");
        user.setSurname("Surname");
        user.setAddress("Address");
        user.setPhone("123456789");
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
