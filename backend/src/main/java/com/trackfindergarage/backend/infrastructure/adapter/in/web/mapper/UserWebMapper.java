package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper web para transformar usuarios de dominio en respuestas HTTP.
 *
 * <p>Se centra en la conversión a {@link UserResponse}. La preparación del usuario, como fijar el
 * rol, la fecha de creación o el hash de contraseña, permanece en la capa de aplicación.</p>
 */
@Component
public class UserWebMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .created(user.getCreated())
                .enabled(user.getEnabled())
                .name(user.getName())
                .surname(user.getSurname())
                .address(user.getAddress())
                .phone(user.getPhone())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .build();
    }
}
