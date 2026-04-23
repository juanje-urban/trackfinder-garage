package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import org.springframework.stereotype.Component;

/*Nota: el servicio debe hacer la conversión de role a roleId, añadir la fecha de creación y fijar por defecto
enabled=true y transformar el password en passwordHash.
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
