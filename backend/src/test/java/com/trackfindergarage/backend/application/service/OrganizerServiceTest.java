package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.application.port.in.UpdateCurrentOrganizerProfileCommand;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceTest {

    @Mock
    private OrganizerPersistencePort organizerPersistencePort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private RolePersistencePort rolePersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OrganizerService organizerService;

    @Test
    void createOrganizerPersistsUserAndOrganizerWithExpectedDefaults() {
        Organizer organizer = organizerWithUser(null, "promoter");
        Role organizerRole = roleWithId(2L, "ORGANIZER");

        when(userPersistencePort.findByDisplayName("promoter")).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail("promoter@example.com")).thenReturn(Optional.empty());
        when(userPersistencePort.findByPhone("123456789")).thenReturn(Optional.empty());
        when(organizerPersistencePort.findByLegalName("Track Finder S.L.")).thenReturn(Optional.empty());
        when(organizerPersistencePort.findByCif("B12345678")).thenReturn(Optional.empty());
        when(rolePersistencePort.findByRoleName("ORGANIZER")).thenReturn(Optional.of(organizerRole));
        when(passwordEncoder.encode("plain-pass")).thenReturn("hashed-pass");
        when(userPersistencePort.save(organizer.getUser())).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(8L);
            return savedUser;
        });
        when(organizerPersistencePort.save(organizer)).thenAnswer(invocation -> {
            Organizer savedOrganizer = invocation.getArgument(0);
            savedOrganizer.setIdUser(savedOrganizer.getUser().getId());
            return savedOrganizer;
        });

        Organizer createdOrganizer = organizerService.createOrganizer(organizer, "plain-pass");

        assertSame(organizer, createdOrganizer);
        assertEquals(8L, createdOrganizer.getIdUser());
        assertSame(organizerRole, createdOrganizer.getUser().getRole());
        assertEquals("hashed-pass", createdOrganizer.getUser().getPasswordHash());
        assertTrue(createdOrganizer.getUser().getEnabled());
        assertEquals(Boolean.FALSE, createdOrganizer.getEnabled());
        assertNotNull(createdOrganizer.getUser().getCreated());
        verify(userPersistencePort).save(organizer.getUser());
        verify(organizerPersistencePort).save(organizer);
    }

    @Test
    void createOrganizerThrowsWhenLegalNameAlreadyExists() {
        Organizer organizer = organizerWithUser(null, "promoter");

        when(userPersistencePort.findByDisplayName("promoter")).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail("promoter@example.com")).thenReturn(Optional.empty());
        when(userPersistencePort.findByPhone("123456789")).thenReturn(Optional.empty());
        when(organizerPersistencePort.findByLegalName("Track Finder S.L."))
                .thenReturn(Optional.of(new Organizer()));

        assertThrows(DuplicateResourceException.class, () -> organizerService.createOrganizer(organizer, "plain-pass"));
        verify(userPersistencePort, never()).save(organizer.getUser());
    }

    @Test
    void getCurrentOrganizerReturnsOrganizerForAuthenticatedEmail() {
        Organizer existingOrganizer = organizerWithUser(5L, "promoter");
        existingOrganizer.getUser().setEmail("promoter@example.com");

        when(userPersistencePort.findByEmail("promoter@example.com"))
                .thenReturn(Optional.of(existingOrganizer.getUser()));
        when(organizerPersistencePort.findById(5L)).thenReturn(Optional.of(existingOrganizer));

        Organizer currentOrganizer = organizerService.getCurrentOrganizer("promoter@example.com");

        assertSame(existingOrganizer, currentOrganizer);
    }

    @Test
    void updateCurrentOrganizerProfileCopiesEditableFieldsAndPassword() {
        Organizer existingOrganizer = organizerWithUser(5L, "promoter");
        existingOrganizer.getUser().setEmail("promoter@example.com");

        when(userPersistencePort.findByEmail("promoter@example.com"))
                .thenReturn(Optional.of(existingOrganizer.getUser()));
        when(organizerPersistencePort.findById(5L)).thenReturn(Optional.of(existingOrganizer));
        when(userPersistencePort.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userPersistencePort.findByPhone("777")).thenReturn(Optional.empty());
        when(organizerPersistencePort.findByLegalName("New Legal")).thenReturn(Optional.empty());
        when(organizerPersistencePort.findByCif("B99999999")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("new-secret")).thenReturn("hashed-secret");
        when(organizerPersistencePort.save(existingOrganizer)).thenReturn(existingOrganizer);

        Organizer updatedOrganizer = organizerService.updateCurrentOrganizerProfile(
                "promoter@example.com",
                new UpdateCurrentOrganizerProfileCommand(
                        "New",
                        "Organizer",
                        "new@example.com",
                        "New address",
                        "777",
                        "New Legal",
                        "B99999999",
                        "new-secret"
                )
        );

        assertSame(existingOrganizer, updatedOrganizer);
        assertEquals("New", existingOrganizer.getUser().getName());
        assertEquals("Organizer", existingOrganizer.getUser().getSurname());
        assertEquals("new@example.com", existingOrganizer.getUser().getEmail());
        assertEquals("New address", existingOrganizer.getUser().getAddress());
        assertEquals("777", existingOrganizer.getUser().getPhone());
        assertEquals("hashed-secret", existingOrganizer.getUser().getPasswordHash());
        assertEquals("New Legal", existingOrganizer.getLegalName());
        assertEquals("B99999999", existingOrganizer.getCif());
        verify(userPersistencePort).save(existingOrganizer.getUser());
        verify(organizerPersistencePort).save(existingOrganizer);
    }

    @Test
    void deleteOrganizerRemovesOrganizerAndUser() {
        Organizer existingOrganizer = organizerWithUser(6L, "promoter");

        when(organizerPersistencePort.findById(6L)).thenReturn(Optional.of(existingOrganizer));

        organizerService.deleteOrganizer(6L);

        verify(organizerPersistencePort).delete(existingOrganizer);
        verify(userPersistencePort).delete(existingOrganizer.getUser());
    }

    @Test
    void getAllOrganizersReturnsPersistenceResult() {
        List<Organizer> organizers = List.of(organizerWithUser(1L, "one"), organizerWithUser(2L, "two"));

        when(organizerPersistencePort.findAll()).thenReturn(organizers);

        assertEquals(organizers, organizerService.getAllOrganizers());
    }

    @Test
    void enableOrganizerMarksOrganizerAsEnabled() {
        Organizer existingOrganizer = organizerWithUser(3L, "promoter");
        existingOrganizer.setEnabled(false);

        when(organizerPersistencePort.findById(3L)).thenReturn(Optional.of(existingOrganizer));
        when(organizerPersistencePort.save(existingOrganizer)).thenReturn(existingOrganizer);

        Organizer enabledOrganizer = organizerService.enableOrganizer(3L);

        assertTrue(enabledOrganizer.getEnabled());
        verify(organizerPersistencePort).save(existingOrganizer);
    }

    @Test
    void createOrganizerThrowsWhenPhoneAlreadyExists() {
        Organizer organizer = organizerWithUser(null, "promoter");

        when(userPersistencePort.findByDisplayName("promoter")).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail("promoter@example.com")).thenReturn(Optional.empty());
        when(userPersistencePort.findByPhone("123456789")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () -> organizerService.createOrganizer(organizer, "plain-pass"));
        verify(userPersistencePort, never()).save(organizer.getUser());
    }

    private Organizer organizerWithUser(Long idUser, String displayName) {
        User user = new User();
        user.setId(idUser);
        user.setDisplayName(displayName);
        user.setEmail(displayName + "@example.com");
        user.setName("Name");
        user.setSurname("Surname");
        user.setAddress("Address");
        user.setPhone("123456789");

        Organizer organizer = new Organizer();
        organizer.setIdUser(idUser);
        organizer.setUser(user);
        organizer.setLegalName("Track Finder S.L.");
        organizer.setCif("B12345678");
        organizer.setEnabled(true);
        return organizer;
    }

    private Role roleWithId(Long id, String value) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(value);
        return role;
    }
}
