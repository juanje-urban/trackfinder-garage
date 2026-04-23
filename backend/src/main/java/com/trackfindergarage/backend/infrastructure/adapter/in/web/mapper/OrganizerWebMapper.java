package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import org.springframework.stereotype.Component;

/* Nota: el servicio debe fijar el rol ORGANIZER, añadir la fecha de creación del user,
establecer por defecto user.enabled=true y organizer.enabled=false, y transformar
el password en passwordHash.
 */

@Component
public class OrganizerWebMapper {

    public Organizer toDomain(CreateOrganizerRequest request) {
        User user = new User();
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        Organizer organizer = new Organizer();
        organizer.setUser(user);
        organizer.setLegalName(request.getLegalName());
        organizer.setCif(request.getCif());

        return organizer;
    }

    public OrganizerResponse toResponse(Organizer organizer) {
        User user = organizer.getUser();

        return OrganizerResponse.builder()
                .idUser(organizer.getIdUser())
                .displayName(user != null ? user.getDisplayName() : null)
                .email(user != null ? user.getEmail() : null)
                .name(user != null ? user.getName() : null)
                .surname(user != null ? user.getSurname() : null)
                .address(user != null ? user.getAddress() : null)
                .phone(user != null ? user.getPhone() : null)
                .created(user != null ? user.getCreated() : null)
                .userEnabled(user != null ? user.getEnabled() : null)
                .roleId(user != null && user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user != null && user.getRole() != null ? user.getRole().getRoleName() : null)
                .legalName(organizer.getLegalName())
                .cif(organizer.getCif())
                .organizerEnabled(organizer.getEnabled())
                .build();
    }
}
