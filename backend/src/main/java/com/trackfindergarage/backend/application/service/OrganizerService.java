package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrganizerService implements OrganizerUseCase {

    private final OrganizerPersistencePort organizerPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final RolePersistencePort rolePersistencePort;
    private final PasswordEncoder passwordEncoder;

    public OrganizerService(OrganizerPersistencePort organizerPersistencePort,
                            UserPersistencePort userPersistencePort,
                            RolePersistencePort rolePersistencePort,
                            PasswordEncoder passwordEncoder) {
        this.organizerPersistencePort = organizerPersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.rolePersistencePort = rolePersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Organizer createOrganizer(Organizer organizer, String rawPassword) {
        User user = organizer.getUser();

        validateDisplayNameForCreate(user.getDisplayName());
        validateEmailForCreate(user.getEmail());
        validateLegalNameForCreate(organizer.getLegalName());
        validateCifForCreate(organizer.getCif());

        Role organizerRole = getOrganizerRole();

        user.setRole(organizerRole);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);

        User savedUser = userPersistencePort.save(user);

        organizer.setUser(savedUser);
        organizer.setEnabled(false);

        return organizerPersistencePort.save(organizer);
    }

    @Override
    public Organizer updateOrganizer(Long id, Organizer organizer) {
        Organizer existingOrganizer = getOrganizerById(id);
        User existingUser = existingOrganizer.getUser();
        User user = organizer.getUser();

        validateDisplayNameForUpdate(id, user.getDisplayName());
        validateEmailForUpdate(id, user.getEmail());
        validateLegalNameForUpdate(id, organizer.getLegalName());
        validateCifForUpdate(id, organizer.getCif());

        existingUser.setDisplayName(user.getDisplayName());
        existingUser.setEmail(user.getEmail());
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhone(user.getPhone());

        existingOrganizer.setLegalName(organizer.getLegalName());
        existingOrganizer.setCif(organizer.getCif());

        userPersistencePort.save(existingUser);
        return organizerPersistencePort.save(existingOrganizer);
    }

    @Override
    public void deleteOrganizer(Long id) {
        Organizer existingOrganizer = getOrganizerById(id);
        User existingUser = existingOrganizer.getUser();

        organizerPersistencePort.delete(existingOrganizer);
        userPersistencePort.delete(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organizer> getAllOrganizers() {
        return organizerPersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Organizer getOrganizerById(Long id) {
        return organizerPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + id));
    }

    @Override
    public Organizer enableOrganizer(Long id) {
        Organizer existingOrganizer = getOrganizerById(id);
        existingOrganizer.setEnabled(true);
        return organizerPersistencePort.save(existingOrganizer);
    }

    @Override
    public Organizer disableOrganizer(Long id) {
        Organizer existingOrganizer = getOrganizerById(id);
        existingOrganizer.setEnabled(false);
        return organizerPersistencePort.save(existingOrganizer);
    }

    private Role getOrganizerRole() {
        return rolePersistencePort.findByRoleName("ORGANIZER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ORGANIZER"));
    }

    private void validateDisplayNameForCreate(String displayName) {
        userPersistencePort.findByDisplayName(displayName)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(
                            "User with display name '" + displayName + "' already exists"
                    );
                });
    }

    private void validateEmailForCreate(String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(
                            "User with email '" + email + "' already exists"
                    );
                });
    }

    private void validateLegalNameForCreate(String legalName) {
        organizerPersistencePort.findByLegalName(legalName)
                .ifPresent(existingOrganizer -> {
                    throw new DuplicateResourceException(
                            "Organizer with legal name '" + legalName + "' already exists"
                    );
                });
    }

    private void validateCifForCreate(String cif) {
        organizerPersistencePort.findByCif(cif)
                .ifPresent(existingOrganizer -> {
                    throw new DuplicateResourceException(
                            "Organizer with cif '" + cif + "' already exists"
                    );
                });
    }

    private void validateDisplayNameForUpdate(Long organizerId, String displayName) {
        userPersistencePort.findByDisplayName(displayName)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(organizerId)) {
                        throw new DuplicateResourceException(
                                "User with display name '" + displayName + "' already exists"
                        );
                    }
                });
    }

    private void validateEmailForUpdate(Long organizerId, String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(organizerId)) {
                        throw new DuplicateResourceException(
                                "User with email '" + email + "' already exists"
                        );
                    }
                });
    }

    private void validateLegalNameForUpdate(Long organizerId, String legalName) {
        organizerPersistencePort.findByLegalName(legalName)
                .ifPresent(existingOrganizer -> {
                    if (!existingOrganizer.getIdUser().equals(organizerId)) {
                        throw new DuplicateResourceException(
                                "Organizer with legal name '" + legalName + "' already exists"
                        );
                    }
                });
    }

    private void validateCifForUpdate(Long organizerId, String cif) {
        organizerPersistencePort.findByCif(cif)
                .ifPresent(existingOrganizer -> {
                    if (!existingOrganizer.getIdUser().equals(organizerId)) {
                        throw new DuplicateResourceException(
                                "Organizer with cif '" + cif + "' already exists"
                        );
                    }
                });
    }
}