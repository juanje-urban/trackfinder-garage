package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserWebMapperTest {

    private final UserWebMapper userWebMapper = new UserWebMapper();

    @Test
    void toDomainMapsCreateRequestToUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setDisplayName("Juanje");
        request.setEmail("juanje@example.com");
        request.setName("Juanje");
        request.setSurname("Urban");
        request.setAddress("Street");
        request.setPhone("123");

        User user = userWebMapper.toDomain(request);

        assertEquals("Juanje", user.getDisplayName());
        assertEquals("juanje@example.com", user.getEmail());
        assertEquals("Juanje", user.getName());
        assertEquals("Urban", user.getSurname());
        assertEquals("Street", user.getAddress());
        assertEquals("123", user.getPhone());
    }

    @Test
    void toResponseMapsUserToResponseIncludingRoleId() {
        Role role = new Role();
        role.setId(7L);

        User user = new User();
        user.setId(8L);
        user.setDisplayName("Juanje");
        user.setEmail("juanje@example.com");
        user.setCreated(LocalDateTime.of(2026, 3, 21, 10, 0));
        user.setEnabled(true);
        user.setName("Juanje");
        user.setSurname("Urban");
        user.setAddress("Street");
        user.setPhone("123");
        user.setRole(role);

        UserResponse response = userWebMapper.toResponse(user);

        assertEquals(8L, response.getId());
        assertEquals("Juanje", response.getDisplayName());
        assertEquals("juanje@example.com", response.getEmail());
        assertEquals(7L, response.getRoleId());
    }

    @Test
    void toResponseLeavesRoleIdNullWhenRoleIsMissing() {
        User user = new User();
        user.setId(1L);

        UserResponse response = userWebMapper.toResponse(user);

        assertNull(response.getRoleId());
    }
}
