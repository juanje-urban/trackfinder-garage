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

/**
 * Implementa la lógica de gestion de organizadores.
 *
 * <p>Coordina la creación y actualización del organizador junto con su usuario asociado, aplicando validaciones
 * tanto sobre los datos comunes del usuario como sobre los datos legales propios del organizador.</p>
 */
@Service
@Transactional
public class OrganizerService implements OrganizerUseCase {

    private static final String ORGANIZER_NOT_FOUND_WITH_ID = "Organizador no encontrado con id: ";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";
    private static final String USER_DISPLAY_NAME_ALREADY_EXISTS = "Ya existe un usuario con el nombre visible '%s'";
    private static final String USER_EMAIL_ALREADY_EXISTS = "Ya existe un usuario con el correo electrónico '%s'";
    private static final String USER_PHONE_ALREADY_EXISTS = "Ya existe un usuario con el teléfono '%s'";
    private static final String ORGANIZER_LEGAL_NAME_ALREADY_EXISTS = "Ya existe un organizador con la razón social '%s'";
    private static final String ORGANIZER_CIF_ALREADY_EXISTS = "Ya existe un organizador con el CIF '%s'";

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

    /**
     * Crea un nuevo organizador y el usuario asociado.
     *
     * <p>La cuenta de usuario se crea con rol {@code ORGANIZER}, contraseña codificada y estado habilitado. En
     * cambio, la ficha de organizador nace deshabilitada para reflejar la pendiente de aprobación.</p>
     *
     * @param organizer datos del organizador
     * @param rawPassword contraseña en texto plano del usuario asociado
     * @return organizador persistido
     */
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

    /**
     * Recupera el organizador vinculado al usuario autenticado.
     *
     * @param authenticatedEmail email resuelto por la capa de seguridad
     * @return organizador autenticado
     */
    @Override
    @Transactional(readOnly = true)
    public Organizer getCurrentOrganizer(String authenticatedEmail) {
        return findOrganizerByAuthenticatedEmail(authenticatedEmail);
    }

    /**
     * Actualiza el perfil del organizador autenticado y de su usuario asociado.
     *
     * @param authenticatedEmail email del usuario autenticado
     * @param profileCommand comando con los nuevos datos del perfil
     * @return organizador actualizado
     */
    @Override
    public Organizer updateCurrentOrganizerProfile(String authenticatedEmail,
                                                   UpdateCurrentOrganizerProfileCommand profileCommand) {
        Organizer existingOrganizer = findOrganizerByAuthenticatedEmail(authenticatedEmail);
        User existingUser = existingOrganizer.getUser();

        String normalizedName = normalizeText(profileCommand.name());
        String normalizedSurname = normalizeText(profileCommand.surname());
        String normalizedEmail = normalizeEmail(profileCommand.email());
        String normalizedAddress = normalizeText(profileCommand.address());
        String normalizedPhone = normalizeText(profileCommand.phone());
        String normalizedLegalName = normalizeText(profileCommand.legalName());
        String normalizedCif = normalizeText(profileCommand.cif());

        validateEmailForUpdate(existingUser.getId(), normalizedEmail);
        validatePhoneForUpdate(existingUser.getId(), normalizedPhone);
        validateLegalNameForUpdate(existingOrganizer.getIdUser(), normalizedLegalName);
        validateCifForUpdate(existingOrganizer.getIdUser(), normalizedCif);

        applyUserProfile(existingUser, normalizedName, normalizedSurname, normalizedEmail, normalizedAddress, normalizedPhone);
        if (profileCommand.rawPassword() != null && !profileCommand.rawPassword().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(profileCommand.rawPassword().trim()));
        }

        applyOrganizerIdentity(existingOrganizer, normalizedLegalName, normalizedCif);
        userPersistencePort.save(existingUser);
        return organizerPersistencePort.save(existingOrganizer);
    }

    /**
     * Elimina un organizador y el usuario asociado a su cuenta.
     *
     * @param id identificador del organizador
     */
    @Override
    public void deleteOrganizer(Long id) {
        Organizer existingOrganizer = findOrganizerOrThrow(id);
        User existingUser = existingOrganizer.getUser();

        organizerPersistencePort.delete(existingOrganizer);
        userPersistencePort.delete(existingUser);
    }

    /**
     * Recupera todos los organizadores registrados.
     *
     * @return listado completo de organizadores
     */
    @Override
    @Transactional(readOnly = true)
    public List<Organizer> getAllOrganizers() {
        return organizerPersistencePort.findAll();
    }

    /**
     * Habilita una cuenta de organizador.
     *
     * @param id identificador del organizador
     * @return organizador habilitado
     */
    @Override
    public Organizer enableOrganizer(Long id) {
        Organizer existingOrganizer = findOrganizerOrThrow(id);
        existingOrganizer.setEnabled(true);
        return organizerPersistencePort.save(existingOrganizer);
    }

    // Obtiene el rol que identifica a las cuentas de organizador.
    private Role getOrganizerRole() {
        return rolePersistencePort.findByRoleName("ORGANIZER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ORGANIZER"));
    }

    // Valida unicidad del alias visible durante el alta.
    private void validateDisplayNameForCreate(String displayName) {
        validateUniqueUser(
                userPersistencePort.findByDisplayName(displayName),
                null,
                USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName)
        );
    }

    // Valida unicidad del correo durante el alta.
    private void validateEmailForCreate(String email) {
        validateUniqueUser(
                userPersistencePort.findByEmail(email),
                null,
                USER_EMAIL_ALREADY_EXISTS.formatted(email)
        );
    }

    // Valida unicidad del teléfono durante el alta.
    private void validatePhoneForCreate(String phone) {
        validateUniqueUser(
                userPersistencePort.findByPhone(phone),
                null,
                USER_PHONE_ALREADY_EXISTS.formatted(phone)
        );
    }

    // Valida unicidad de la razón social durante el alta.
    private void validateLegalNameForCreate(String legalName) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByLegalName(legalName),
                null,
                ORGANIZER_LEGAL_NAME_ALREADY_EXISTS.formatted(legalName)
        );
    }

    // Valida unicidad del CIF durante el alta.
    private void validateCifForCreate(String cif) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByCif(cif),
                null,
                ORGANIZER_CIF_ALREADY_EXISTS.formatted(cif)
        );
    }

    // Valida unicidad del correo permitiendo conservar el del propio organizador.
    private void validateEmailForUpdate(Long organizerId, String email) {
        validateUniqueUser(
                userPersistencePort.findByEmail(email),
                organizerId,
                USER_EMAIL_ALREADY_EXISTS.formatted(email)
        );
    }

    // Valida unicidad del teléfono permitiendo conservar el del propio organizador.
    private void validatePhoneForUpdate(Long organizerId, String phone) {
        validateUniqueUser(
                userPersistencePort.findByPhone(phone),
                organizerId,
                USER_PHONE_ALREADY_EXISTS.formatted(phone)
        );
    }

    // Valida unicidad de la razón social permitiendo conservar la propia.
    private void validateLegalNameForUpdate(Long organizerId, String legalName) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByLegalName(legalName),
                organizerId,
                ORGANIZER_LEGAL_NAME_ALREADY_EXISTS.formatted(legalName)
        );
    }

    // Valida unicidad del CIF permitiendo conservar el propio.
    private void validateCifForUpdate(Long organizerId, String cif) {
        validateUniqueOrganizer(
                organizerPersistencePort.findByCif(cif),
                organizerId,
                ORGANIZER_CIF_ALREADY_EXISTS.formatted(cif)
        );
    }

    // Reutiliza el mismo control de duplicados para los datos de usuario.
    private void validateUniqueUser(Optional<User> candidate,
                                    Long excludedUserId,
                                    String duplicateMessage) {
        candidate.ifPresent(existingUser -> {
            if (excludedUserId == null || !existingUser.getId().equals(excludedUserId)) {
                throw new DuplicateResourceException(duplicateMessage);
            }
        });
    }

    // Reutiliza el mismo control de duplicados para los datos legales del organizador.
    private void validateUniqueOrganizer(Optional<Organizer> candidate,
                                         Long excludedOrganizerId,
                                         String duplicateMessage) {
        candidate.ifPresent(existingOrganizer -> {
            if (excludedOrganizerId == null || !existingOrganizer.getIdUser().equals(excludedOrganizerId)) {
                throw new DuplicateResourceException(duplicateMessage);
            }
        });
    }

    // Carga un organizador por id o centraliza el error de no encontrado.
    private Organizer findOrganizerOrThrow(Long id) {
        return organizerPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + id));
    }

    // Resuelve el organizador asociado al usuario autenticado.
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

    // Aplica los datos que sólo deben fijarse en creación.
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

    // Aplica los datos editables del usuario asociado al organizador.
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

    // Aplica los datos legales editables del organizador.
    private void applyOrganizerIdentity(Organizer organizer, String legalName, String cif) {
        organizer.setLegalName(legalName);
        organizer.setCif(cif);
    }

    // Normaliza textos opcionales conservando null cuando el campo no viene informado.
    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    // Normaliza el correo para búsquedas y validaciones.
    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
