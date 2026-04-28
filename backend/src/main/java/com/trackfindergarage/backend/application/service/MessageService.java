package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.MessageUseCase;
import com.trackfindergarage.backend.application.port.out.MessagePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Implementa la lógica de mensajería entre usuarios.
 *
 * <p>Valida remitente, destinatario, permisos de lectura y longitud de los textos antes de
 * persistir los mensajes.</p>
 */
@org.springframework.stereotype.Service
@Transactional
public class MessageService implements MessageUseCase {

    private static final String MESSAGE_NOT_FOUND_WITH_ID = "Mensaje no encontrado con id: ";
    private static final String USER_NOT_FOUND_WITH_ID = "Usuario no encontrado con id: ";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";
    private static final String SUBJECT_REQUIRED = "El asunto es obligatorio";
    private static final String MESSAGE_TEXT_REQUIRED = "El texto del mensaje es obligatorio";
    private static final String SUBJECT_TOO_LONG = "El asunto no debe superar los 255 caracteres";
    private static final String MESSAGE_TEXT_TOO_LONG = "El texto del mensaje no debe superar los 500 caracteres";
    private static final String USER_CANNOT_MESSAGE_SELF = "Un usuario no puede enviarse un mensaje a sí mismo";
    private static final String ONLY_RECEIVER_CAN_CHANGE_READ_STATUS =
            "Solo el destinatario puede cambiar el estado de lectura de un mensaje";
    private static final String RECEIVER_ACCOUNT_IS_DISABLED =
            "La cuenta del destinatario debe estar activa para recibir nuevos mensajes";

    private final MessagePersistencePort messagePersistencePort;
    private final UserPersistencePort userPersistencePort;

    public MessageService(MessagePersistencePort messagePersistencePort, UserPersistencePort userPersistencePort) {
        this.messagePersistencePort = messagePersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    /**
     * Recupera todos los mensajes en los que participa el usuario.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de mensajes ordenados cronológicamente
     */
    @Override
    @Transactional(readOnly = true)
    public List<Message> getOwnMessages(String authenticatedEmail) {
        User currentUser = loadAuthenticatedUser(authenticatedEmail);
        return messagePersistencePort.findByParticipantIdOrderBySentAtAsc(currentUser.getId());
    }

    /**
     * Envía un nuevo mensaje desde el usuario a otro usuario activo.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param receiverId identificador del destinatario
     * @param subject asunto del mensaje
     * @param content contenido del mensaje
     * @return mensaje creado
     */
    @Override
    public Message createOwnMessage(String authenticatedEmail, Long receiverId, String subject, String content) {
        User sender = loadAuthenticatedUser(authenticatedEmail);
        User receiver = loadUserById(receiverId);

        // Impide conversaciones triviales del usuario consigo mismo.
        if (Objects.equals(sender.getId(), receiver.getId())) {
            throw new IllegalArgumentException(USER_CANNOT_MESSAGE_SELF);
        }
        // Obliga a que el destinatario siga activo antes de aceptar mensajes nuevos.
        if (!Boolean.TRUE.equals(receiver.getEnabled())) {
            throw new IllegalArgumentException(RECEIVER_ACCOUNT_IS_DISABLED);
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject(subject);
        message.setContent(content);
        validateMessage(message);

        return saveNewMessage(message, sender, receiver);
    }

    /**
     * Marca como leído un mensaje recibido por el usuario.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador del mensaje
     * @return mensaje actualizado
     */
    @Override
    public Message markOwnMessageAsRead(String authenticatedEmail, Long id) {
        User currentUser = loadAuthenticatedUser(authenticatedEmail);
        Message message = findMessageOrThrow(id);
        validateReceiverAccess(message, currentUser.getId());
        message.setIsRead(true);
        return messagePersistencePort.save(message);
    }

    /**
     * Recupera los usuarios activos a los que el usuario puede enviar mensajes.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de destinatarios disponibles
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAvailableRecipients(String authenticatedEmail) {
        User currentUser = loadAuthenticatedUser(authenticatedEmail);

        return userPersistencePort.findAll()
                .stream()
                .filter(user -> !Objects.equals(user.getId(), currentUser.getId()))
                .filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .sorted(Comparator.comparing(User::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // Valida el asunto y el contenido antes de persistir un mensaje nuevo.
    private void validateMessage(Message message) {
        if (message.getSubject() == null || message.getSubject().isBlank()) {
            throw new IllegalArgumentException(SUBJECT_REQUIRED);
        }
        if (message.getSubject().length() > 255) {
            throw new IllegalArgumentException(SUBJECT_TOO_LONG);
        }
        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new IllegalArgumentException(MESSAGE_TEXT_REQUIRED);
        }
        if (message.getContent().length() > 500) {
            throw new IllegalArgumentException(MESSAGE_TEXT_TOO_LONG);
        }
    }

    // Comprueba que solo el destinatario pueda cambiar el estado de lectura.
    private void validateReceiverAccess(Message message, Long userId) {
        loadUserById(userId);
        if (!message.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException(ONLY_RECEIVER_CAN_CHANGE_READ_STATUS);
        }
    }

    // Carga y normaliza el usuario autenticado a partir de su correo.
    private User loadAuthenticatedUser(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = normalizeEmail(authenticatedEmail);
        return userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));
    }

    // Carga un usuario por id o lanza error si no existe.
    private User loadUserById(Long userId) {
        return userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));
    }

    // Normaliza el correo para comparaciones y búsquedas.
    private String normalizeEmail(String authenticatedEmail) {
        return authenticatedEmail.trim().toLowerCase(Locale.ROOT);
    }

    // Completa los metadatos del mensaje antes de guardarlo.
    private Message saveNewMessage(Message message, User sender, User receiver) {
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);
        return messagePersistencePort.save(message);
    }

    // Recupera un mensaje por id o lanza error si no existe.
    private Message findMessageOrThrow(Long id) {
        return messagePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND_WITH_ID + id));
    }

}
