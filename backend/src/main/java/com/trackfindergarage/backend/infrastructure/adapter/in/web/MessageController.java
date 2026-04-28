package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.MessageUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOwnMessageRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageContactResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.MessageWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints HTTP relacionados con la mensajería privada entre usuarios.
 */
@RestController
@RequestMapping("/messages")
public class MessageController extends AbstractWebController {

    private final MessageUseCase messageUseCase;
    private final MessageWebMapper messageWebMapper;

    public MessageController(MessageUseCase messageUseCase, MessageWebMapper messageWebMapper) {
        this.messageUseCase = messageUseCase;
        this.messageWebMapper = messageWebMapper;
    }

    /**
     * Envía un mensaje nuevo.
     *
     * @param authentication autenticación del usuario actual
     * @param request petición con los datos del mensaje
     * @return respuesta con el mensaje creado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(Authentication authentication,
                                         @Valid @RequestBody CreateOwnMessageRequest request) {
        return messageWebMapper.toResponse(
                messageUseCase.createOwnMessage(
                        authenticatedEmail(authentication),
                        request.getReceiverId(),
                        request.getSubject(),
                        request.getMessage()
                )
        );
    }

    /**
     * Recupera la bandeja de mensajes del usuario.
     *
     * @param authentication autenticación del usuario actual
     * @return listado de mensajes
     */
    @GetMapping
    public List<MessageResponse> getOwnMessages(Authentication authentication) {
        return mapResponses(messageUseCase.getOwnMessages(authenticatedEmail(authentication)), messageWebMapper::toResponse);
    }

    /**
     * Recupera los posibles destinatarios para el usuario.
     *
     * @param authentication autenticación del usuario actual
     * @return listado de contactos disponibles
     */
    @GetMapping("/contacts")
    public List<MessageContactResponse> getAvailableRecipients(Authentication authentication) {
        return mapResponses(
                messageUseCase.getAvailableRecipients(authenticatedEmail(authentication)),
                messageWebMapper::toContactResponse
        );
    }

    /**
     * Marca como leído un mensaje recibido por el usuario.
     *
     * @param id identificador del mensaje
     * @param authentication autenticación del usuario actual
     * @return respuesta con el mensaje actualizado
     */
    @PatchMapping("/{id}/read")
    public MessageResponse markAsRead(@PathVariable Long id, Authentication authentication) {
        return messageWebMapper.toResponse(
                messageUseCase.markOwnMessageAsRead(authenticatedEmail(authentication), id)
        );
    }
}
