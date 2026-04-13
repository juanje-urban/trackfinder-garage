package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.application.port.in.UpdateCurrentOrganizerProfileCommand;
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
import java.util.Locale;

@Service
@Transactional
public class OrganizerService implements OrganizerUseCase {

    private static final String ORGANIZER_NOT_FOUND_WITH_ID = "Organizer not found with id: ";
    private static final String USER_DISPLAY_NAME_ALREADY_EXISTS = "User with display name '%s' already exists";
    private static final String USER_EMAIL_ALREADY_EXISTS = "User with email '%s' already exists";
    private static final String USER_PHONE_ALREADY_EXISTS = "User with phone '%s' already exists";
    private static final String ORGANIZER_LEGAL_NAME_ALREADY_EXISTS = "Organizer with legal name '%s' already exists";
    private static final String ORGANIZER_CIF_ALREADY_EXISTS = "Organizer with cif '%s' already exists";

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
        validatePhoneForCreate(user.getPhone());
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
    @Transactional(readOnly = true)
    public Organizer getCurrentOrganizer(String authenticatedEmail) {
        return findOrganizerByAuthenticatedEmail(authenticatedEmail);
    }

    @Override
    public Organizer updateCurrentOrganizerProfile(String authenticatedEmail,
                                                   UpdateCurrentOrganizerProfileCommand command) {
        Organizer existingOrganizer = findOrganizerByAuthenticatedEmail(authenticatedEmail);
        User existingUser = existingOrganizer.getUser();

        String normalizedName = normalizeText(command.name());
        String normalizedSurname = normalizeText(command.surname());
        String normalizedEmail = normalizeEmail(command.email());
        String normalizedAddress = normalizeText(command.address());
        String normalizedPhone = normalizeText(command.phone());
        String normalizedLegalName = normalizeText(command.legalName());
        String normalizedCif = normalizeText(command.cif());

        validateEmailForUpdate(existingUser.getId(), normalizedEmail);
        validatePhoneForUpdate(existingUser.getId(), normalizedPhone);
        validateLegalNameForUpdate(existingOrganizer.getIdUser(), normalizedLegalName);
        validateCifForUpdate(existingOrganizer.getIdUser(), normalizedCif);

        existingUser.setName(normalizedName);
        existingUser.setSurname(normalizedSurname);
        existingUser.setEmail(normalizedEmail);
        existingUser.setAddress(normalizedAddress);
        existingUser.setPhone(normalizedPhone);

        if (command.rawPassword() != null && !command.rawPassword().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(command.rawPassword().trim()));
        }

        existingOrganizer.setLegalName(normalizedLegalName);
        existingOrganizer.setCif(normalizedCif);

        userPersistencePort.save(existingUser);
        return organizerPersistencePort.save(existingOrganizer);
    }

    @Override
    public Organizer updateOrganizer(Long id, Organizer organizer) {
        Organizer existingOrganizer = findOrganizerOrThrow(id);
        User existingUser = existingOrganizer.getUser();
        User user = organizer.getUser();

        validateDisplayNameForUpdate(id, user.getDisplayName());
        validateEmailForUpdate(id, user.getEmail());
        validatePhoneForUpdate(id, user.getPhone());
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
        Organizer existingOrganizer = findOrganizerOrThrow(id);
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
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public Organizer enableOrganizer(Long id) {
        Organizer existingOrganizer = findOrganizerOrThrow(id);
        existingOrganizer.setEnabled(true);
        return organizerPersistencePort.save(existingOrganizer);
    }

    @Override
    public Organizer disableOrganizer(Long id) {
        Organizer existingOrganizer = findOrganizerOrThrow(id);
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
                    throw new DuplicateResourceException(USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName));
                });
    }

    private void validateEmailForCreate(String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(USER_EMAIL_ALREADY_EXISTS.formatted(email));
                });
    }

    private void validateLegalNameForCreate(String legalName) {
        organizerPersistencePort.findByLegalName(legalName)
                .ifPresent(existingOrganizer -> {
                    throw new DuplicateResourceException(ORGANIZER_LEGAL_NAME_ALREADY_EXISTS.formatted(legalName));
                });
    }

    private void validatePhoneForCreate(String phone) {
        userPersistencePort.findByPhone(phone)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(USER_PHONE_ALREADY_EXISTS.formatted(phone));
                });
    }

    private void validateCifForCreate(String cif) {
        organizerPersistencePort.findByCif(cif)
                .ifPresent(existingOrganizer -> {
                    throw new DuplicateResourceException(ORGANIZER_CIF_ALREADY_EXISTS.formatted(cif));
                });
    }

    private void validateDisplayNameForUpdate(Long organizerId, String displayName) {
        userPersistencePort.findByDisplayName(displayName)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(organizerId)) {
                        throw new DuplicateResourceException(USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName));
                    }
                });
    }

    private void validateEmailForUpdate(Long organizerId, String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(organizerId)) {
                        throw new DuplicateResourceException(USER_EMAIL_ALREADY_EXISTS.formatted(email));
                    }
                });
    }

    private void validateLegalNameForUpdate(Long organizerId, String legalName) {
        organizerPersistencePort.findByLegalName(legalName)
                .ifPresent(existingOrganizer -> {
                    if (!existingOrganizer.getIdUser().equals(organizerId)) {
                        throw new DuplicateResourceException(ORGANIZER_LEGAL_NAME_ALREADY_EXISTS.formatted(legalName));
                    }
                });
    }

    private void validatePhoneForUpdate(Long organizerId, String phone) {
        userPersistencePort.findByPhone(phone)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(organizerId)) {
                        throw new DuplicateResourceException(USER_PHONE_ALREADY_EXISTS.formatted(phone));
                    }
                });
    }

    private void validateCifForUpdate(Long organizerId, String cif) {
        organizerPersistencePort.findByCif(cif)
                .ifPresent(existingOrganizer -> {
                    if (!existingOrganizer.getIdUser().equals(organizerId)) {
                        throw new DuplicateResourceException(ORGANIZER_CIF_ALREADY_EXISTS.formatted(cif));
                    }
                });
    }

    //Función privada que hace lo mismo que getOrganizerById. Los métodos con proxy de Spring no deben ser llamados desde dentro del propio bean. (Da error sonar)
    private Organizer findOrganizerOrThrow(Long id) {
        return organizerPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + id));
    }

    private Organizer findOrganizerByAuthenticatedEmail(String authenticatedEmail) {
        String normalizedEmail = normalizeEmail(authenticatedEmail);

        User user = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

        return organizerPersistencePort.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + user.getId()));
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
