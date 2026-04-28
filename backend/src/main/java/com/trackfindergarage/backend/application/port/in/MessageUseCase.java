package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;

import java.util.List;

/**
 * Define los casos de uso relacionados con la mensajería entre usuarios.
 */
public interface MessageUseCase {

    /**
     * Recupera los mensajes del usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de mensajes del usuario
     */
    List<Message> getOwnMessages(String authenticatedEmail);

    /**
     * Crea un mensaje nuevo enviado por el usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param receiverId identificador del destinatario
     * @param subject asunto del mensaje
     * @param content contenido del mensaje
     * @return mensaje creado
     */
    Message createOwnMessage(String authenticatedEmail, Long receiverId, String subject, String content);

    /**
     * Marca como leído un mensaje recibido por el usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador del mensaje
     * @return mensaje actualizado
     */
    Message markOwnMessageAsRead(String authenticatedEmail, Long id);

    /**
     * Recupera los posibles destinatarios a los que el usuario autenticado puede escribir.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de destinatarios disponibles
     */
    List<User> getAvailableRecipients(String authenticatedEmail);
}
