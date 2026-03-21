package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateOrganizerRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizerWebMapperTest {

    private final OrganizerWebMapper organizerWebMapper = new OrganizerWebMapper();

    @Test
    void toDomainMapsCreateRequestToOrganizerAndUser() {
        CreateOrganizerRequest request = new CreateOrganizerRequest();
        request.setDisplayName("org");
        request.setEmail("org@example.com");
        request.setName("Org");
        request.setSurname("Owner");
        request.setAddress("Address");
        request.setPhone("123");
        request.setLegalName("Org SL");
        request.setCif("B12345678");

        Organizer organizer = organizerWebMapper.toDomain(request);

        assertEquals("org", organizer.getUser().getDisplayName());
        assertEquals("org@example.com", organizer.getUser().getEmail());
        assertEquals("Org SL", organizer.getLegalName());
        assertEquals("B12345678", organizer.getCif());
    }

    @Test
    void updateDomainCreatesUserIfMissingAndMapsFields() {
        Organizer organizer = new Organizer();
        UpdateOrganizerRequest request = new UpdateOrganizerRequest();
        request.setDisplayName("org2");
        request.setEmail("org2@example.com");
        request.setName("Name");
        request.setSurname("Surname");
        request.setAddress("Addr");
        request.setPhone("456");
        request.setLegalName("New Legal");
        request.setCif("B99999999");

        organizerWebMapper.updateDomain(organizer, request);

        assertEquals("org2", organizer.getUser().getDisplayName());
        assertEquals("org2@example.com", organizer.getUser().getEmail());
        assertEquals("New Legal", organizer.getLegalName());
        assertEquals("B99999999", organizer.getCif());
    }

    @Test
    void toResponseMapsOrganizerToResponse() {
        Role role = new Role();
        role.setId(3L);

        User user = new User();
        user.setDisplayName("org");
        user.setEmail("org@example.com");
        user.setName("Name");
        user.setSurname("Surname");
        user.setAddress("Address");
        user.setPhone("123");
        user.setEnabled(true);
        user.setRole(role);

        Organizer organizer = new Organizer();
        organizer.setIdUser(10L);
        organizer.setUser(user);
        organizer.setLegalName("Legal");
        organizer.setCif("B123");
        organizer.setEnabled(false);

        OrganizerResponse response = organizerWebMapper.toResponse(organizer);

        assertEquals(10L, response.getIdUser());
        assertEquals("org", response.getDisplayName());
        assertEquals(3L, response.getRoleId());
        assertEquals(Boolean.FALSE, response.getOrganizerEnabled());
    }

    @Test
    void toResponseHandlesMissingUserGracefully() {
        Organizer organizer = new Organizer();
        organizer.setIdUser(11L);

        OrganizerResponse response = organizerWebMapper.toResponse(organizer);

        assertEquals(11L, response.getIdUser());
        assertNull(response.getDisplayName());
        assertNull(response.getRoleId());
    }
}
