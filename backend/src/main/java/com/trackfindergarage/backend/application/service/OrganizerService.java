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
import java.util.Optional;

@Service
@Transactional
public class OrganizerService implements OrganizerUseCase {

    private static final String ORGANIZER_NOT_FOUND_WITH_ID = "Organizer not found with id: ";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "Authenticated user email is required";
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

        String normalizedDisplayName = normalizeText(user.getDisplayName());
        String normalizedEmail = normalizeEmail(user.getEmail());
        String normalizedName = normalizeText(user.getName());
        String normalizedSurname = normalizeText(user.getSurname());
        String normalizedAddress = normalizeText(user.getAddress());
        String normalizedPhone = normalizeText(user.getPhone());
        String normalizedLegalName = normalizeText(organizer.getLegalName());
        String normalizedCif = normalizeText(organizer.getCif());

        validateDisplayNameForCreate(normalizedDisplayName);
        validateEmailForCreate(normalizedEmail);
        validatePhoneForCreate(normalizedPhone);
        validateLegalNameForCreate(normalizedLegalName);
        validateCifForCreate(normalizedCif);

        applyUserIdentity(
                user,
                normalizedDisplayName,
                normalizedEmail,
                normalizedName,
                normalizedSurname,
                normalizedAddress,
                normalizedPhone
        );
        user.setRole(getOrganizerRole());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);

        User savedUser = userPersistencePort.save(user);
        organizer.setUser(savedUser);
        organizer.setLegalName(normalizedLegalName);
        organizer.setCif(normalizedCif);
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

        applyUserProfile(existingUser, normalizedName, normalizedSurname, normalizedEmail, normalizedAddress, normalizedPhone);
        if (command.rawPassword() != null && !command.rawPassword().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(command.rawPassword().trim()));
        }

        applyOrganizerIdentity(existingOrganizer, normalizedLegalName, normalizedCif);
        userPersistencePort.save(existingUser);
        return organizerPersistencePort.save(existingOrganizer);
    }

    @Override
    public Organizer updateOrganizer(Long id, Organizer organizer) {
        Organizer existingOrganizer = findOrganizerOrThrow(id);
        User existingUser = existingOrganizer.getUser();
        User user = organizer.getUser();

        String normalizedDisplayName = normalizeText(user.getDisplayName());
        String normalizedEmail = normalizeEmail(user.getEmail());
        String normalizedName = normalizeText(user.getName());
        String normalizedSurname = normalizeText(user.getSurname());
        String normalizedAddress = normalizeText(user.getAddress());
        String normalizedPhone = normalizeText(user.getPhone());
        String normalizedLegalName = normalizeText(organizer.getLegalName());
        String normalizedCif = normalizeText(organizer.getCif());

        validateDisplayNameForUpdate(id, normalizedDisplayName);
        validateEmailForUpdate(id, normalizedEmail);
        validatePhoneForUpdate(id, normalizedPhone);
        validateLegalNameForUpdate(id, normalizedLegalName);
        validateCifForUpdate(id, normalizedCif);

        applyUserIdentity(
                existingUser,
                normalizedDisplayName,
                normalizedEmail,
                normalizedName,
                normalizedSurname,
                normalizedAddress,
                normalizedPhone
        );
        applyOrganizerIdentity(existingOrganizer, normalizedLegalName, normalizedCif);

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
        return findOrganizerOrThrow(id);
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
        validateUniqueUser(
                userPersistencePort.findByDisplayName(displayName),
                null,
                USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName)
        );
    }

    private void validateEmailForCreate(String email) {
        validateUniqueUser(
                userPersistencePort.findByEmail(email),
                null,
                USER_EMAIL_ALREADY_EXISTS.formatted(email)
        );
    }

    private void validatePhoneForCreate(String phone) {
        validateUniqueUser(
                userPersistencePort.findByPhone(phone),
                null,
                USER_PHONE_ALREADY_EXISTS.formatted(phone)
        );
    }

    private void validateLegalNameForCreate(String legalName) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByLegalName(legalName),
                null,
                ORGANIZER_LEGAL_NAME_ALREADY_EXISTS.formatted(legalName)
        );
    }

    private void validateCifForCreate(String cif) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByCif(cif),
                null,
                ORGANIZER_CIF_ALREADY_EXISTS.formatted(cif)
        );
    }

    private void validateDisplayNameForUpdate(Long organizerId, String displayName) {
        validateUniqueUser(
                userPersistencePort.findByDisplayName(displayName),
                organizerId,
                USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName)
        );
    }

    private void validateEmailForUpdate(Long organizerId, String email) {
        validateUniqueUser(
                userPersistencePort.findByEmail(email),
                organizerId,
                USER_EMAIL_ALREADY_EXISTS.formatted(email)
        );
    }

    private void validatePhoneForUpdate(Long organizerId, String phone) {
        validateUniqueUser(
                userPersistencePort.findByPhone(phone),
                organizerId,
                USER_PHONE_ALREADY_EXISTS.formatted(phone)
        );
    }

    private void validateLegalNameForUpdate(Long organizerId, String legalName) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByLegalName(legalName),
                organizerId,
                ORGANIZER_LEGAL_NAME_ALREADY_EXISTS.formatted(legalName)
        );
    }

    private void validateCifForUpdate(Long organizerId, String cif) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByCif(cif),
                organizerId,
                ORGANIZER_CIF_ALREADY_EXISTS.formatted(cif)
        );
    }

    private void validateUniqueUser(Optional<User> candidate,
                                    Long excludedUserId,
                                    String duplicateMessage) {
        candidate.ifPresent(existingUser -> {
            if (excludedUserId == null || !existingUser.getId().equals(excludedUserId)) {
                throw new DuplicateResourceException(duplicateMessage);
            }
        });
    }

    private void validateUniqueOrganizer(Optional<Organizer> candidate,
                                         Long excludedOrganizerId,
                                         String duplicateMessage) {
        candidate.ifPresent(existingOrganizer -> {
            if (excludedOrganizerId == null || !existingOrganizer.getIdUser().equals(excludedOrganizerId)) {
                throw new DuplicateResourceException(duplicateMessage);
            }
        });
    }

    private Organizer findOrganizerOrThrow(Long id) {
        return organizerPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + id));
    }

    private Organizer findOrganizerByAuthenticatedEmail(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = normalizeEmail(authenticatedEmail);
        User user = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_WITH_EMAIL + normalizedEmail
                ));

        return organizerPersistencePort.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + user.getId()));
    }

    private void applyUserIdentity(User user,
                                   String displayName,
                                   String email,
                                   String name,
                                   String surname,
                                   String address,
                                   String phone) {
        user.setDisplayName(displayName);
        applyUserProfile(user, name, surname, email, address, phone);
    }

    private void applyUserProfile(User user,
                                  String name,
                                  String surname,
                                  String email,
                                  String address,
                                  String phone) {
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);
    }

    private void applyOrganizerIdentity(Organizer organizer, String legalName, String cif) {
        organizer.setLegalName(legalName);
        organizer.setCif(cif);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
