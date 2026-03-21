package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateOrganizerRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWebMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizerControllerTest {

    private final OrganizerUseCase organizerUseCase = mock(OrganizerUseCase.class);
    private final OrganizerWebMapper organizerWebMapper = new OrganizerWebMapper();
    private final OrganizerController organizerController =
            new OrganizerController(organizerUseCase, organizerWebMapper);

    @Test
    void createOrganizerDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateOrganizerRequest request = new CreateOrganizerRequest();
        request.setDisplayName("org");
        request.setPassword("secret");
        request.setEmail("org@example.com");
        request.setName("Org");
        request.setSurname("Owner");
        request.setAddress("Address");
        request.setPhone("123");
        request.setLegalName("Org SL");
        request.setCif("B12345678");

        when(organizerUseCase.createOrganizer(any(Organizer.class), eq("secret")))
                .thenReturn(organizerWithId(1L, "org"));

        OrganizerResponse response = organizerController.createOrganizer(request);

        assertEquals(1L, response.getIdUser());
        assertEquals("org", response.getDisplayName());
        verify(organizerUseCase).createOrganizer(any(Organizer.class), eq("secret"));
    }

    @Test
    void updateOrganizerDelegatesToUseCaseAndReturnsMappedResponse() {
        UpdateOrganizerRequest request = new UpdateOrganizerRequest();
        request.setDisplayName("org2");
        request.setEmail("org2@example.com");
        request.setName("Name");
        request.setSurname("Surname");
        request.setAddress("Address");
        request.setPhone("999");
        request.setLegalName("Legal");
        request.setCif("B99999999");

        when(organizerUseCase.updateOrganizer(eq(2L), any(Organizer.class)))
                .thenReturn(organizerWithId(2L, "org2"));

        OrganizerResponse response = organizerController.updateOrganizer(2L, request);

        assertEquals(2L, response.getIdUser());
        assertEquals("org2", response.getDisplayName());
        verify(organizerUseCase).updateOrganizer(eq(2L), any(Organizer.class));
    }

    @Test
    void getAllOrganizersMapsUseCaseResult() {
        when(organizerUseCase.getAllOrganizers()).thenReturn(List.of(organizerWithId(1L, "org")));

        List<OrganizerResponse> response = organizerController.getAllOrganizers();

        assertEquals(1, response.size());
        assertEquals("org", response.getFirst().getDisplayName());
    }

    @Test
    void getByIdAndEnableDisableReturnMappedResponses() {
        Organizer organizer = organizerWithId(3L, "org");
        Organizer enabled = organizerWithId(3L, "org");
        enabled.setEnabled(true);
        Organizer disabled = organizerWithId(3L, "org");
        disabled.setEnabled(false);

        when(organizerUseCase.getOrganizerById(3L)).thenReturn(organizer);
        when(organizerUseCase.enableOrganizer(3L)).thenReturn(enabled);
        when(organizerUseCase.disableOrganizer(3L)).thenReturn(disabled);

        OrganizerResponse byId = organizerController.getOrganizerById(3L);
        OrganizerResponse enabledResponse = organizerController.enableOrganizer(3L);
        OrganizerResponse disabledResponse = organizerController.disableOrganizer(3L);

        assertEquals(3L, byId.getIdUser());
        assertTrue(enabledResponse.getOrganizerEnabled());
        assertEquals(Boolean.FALSE, disabledResponse.getOrganizerEnabled());
    }

    @Test
    void deleteOrganizerDelegatesToUseCase() {
        organizerController.deleteOrganizer(4L);

        verify(organizerUseCase).deleteOrganizer(4L);
    }

    private Organizer organizerWithId(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName + "@example.com");
        user.setName("Name");
        user.setSurname("Surname");
        user.setAddress("Address");
        user.setPhone("123");
        user.setEnabled(true);

        Organizer organizer = new Organizer();
        organizer.setIdUser(id);
        organizer.setUser(user);
        organizer.setLegalName("Legal");
        organizer.setCif("B12345678");
        organizer.setEnabled(true);
        return organizer;
    }
}
