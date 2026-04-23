package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizerWebMapperTest {

    private final OrganizerWebMapper organizerWebMapper = new OrganizerWebMapper();

    @Test
    void toDomainMapsCreateRequestToOrganizer() {
        CreateOrganizerRequest request = new CreateOrganizerRequest();
        request.setDisplayName("promoter");
        request.setEmail("promoter@example.com");
        request.setName("Track");
        request.setSurname("Events");
        request.setAddress("Street");
        request.setPhone("123456789");
        request.setLegalName("Track Events S.L.");
        request.setCif("B12345678");

        Organizer organizer = organizerWebMapper.toDomain(request);

        assertEquals("promoter", organizer.getUser().getDisplayName());
        assertEquals("promoter@example.com", organizer.getUser().getEmail());
        assertEquals("Track", organizer.getUser().getName());
        assertEquals("Events", organizer.getUser().getSurname());
        assertEquals("Street", organizer.getUser().getAddress());
        assertEquals("123456789", organizer.getUser().getPhone());
        assertEquals("Track Events S.L.", organizer.getLegalName());
        assertEquals("B12345678", organizer.getCif());
    }

    @Test
    void toResponseMapsOrganizerToResponseIncludingRoleId() {
        Role role = new Role();
        role.setId(7L);
        role.setRoleName("ORGANIZER");

        User user = new User();
        user.setId(8L);
        user.setDisplayName("promoter");
        user.setEmail("promoter@example.com");
        user.setName("Track");
        user.setSurname("Events");
        user.setAddress("Street");
        user.setPhone("123456789");
        user.setCreated(LocalDateTime.of(2026, 4, 23, 10, 0));
        user.setEnabled(true);
        user.setRole(role);

        Organizer organizer = new Organizer();
        organizer.setIdUser(8L);
        organizer.setUser(user);
        organizer.setLegalName("Track Events S.L.");
        organizer.setCif("B12345678");
        organizer.setEnabled(false);

        OrganizerResponse response = organizerWebMapper.toResponse(organizer);

        assertEquals(8L, response.getIdUser());
        assertEquals("promoter", response.getDisplayName());
        assertEquals("promoter@example.com", response.getEmail());
        assertEquals(7L, response.getRoleId());
        assertEquals("ORGANIZER", response.getRoleName());
        assertEquals("Track Events S.L.", response.getLegalName());
        assertEquals("B12345678", response.getCif());
        assertEquals(Boolean.FALSE, response.getOrganizerEnabled());
    }

    @Test
    void toResponseLeavesRoleFieldsNullWhenUserRoleIsMissing() {
        User user = new User();
        user.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setIdUser(1L);
        organizer.setUser(user);

        OrganizerResponse response = organizerWebMapper.toResponse(organizer);

        assertNull(response.getRoleId());
        assertNull(response.getRoleName());
    }
}
