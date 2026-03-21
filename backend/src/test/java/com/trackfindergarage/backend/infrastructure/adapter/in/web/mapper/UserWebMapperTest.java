package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateUserRequest;
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
        request.setDisplayName("juan");
        request.setEmail("juan@example.com");
        request.setName("Juan");
        request.setSurname("Urban");
        request.setAddress("Street");
        request.setPhone("123");

        User user = userWebMapper.toDomain(request);

        assertEquals("juan", user.getDisplayName());
        assertEquals("juan@example.com", user.getEmail());
        assertEquals("Juan", user.getName());
        assertEquals("Urban", user.getSurname());
        assertEquals("Street", user.getAddress());
        assertEquals("123", user.getPhone());
    }

    @Test
    void updateDomainMapsUpdateRequestToExistingUser() {
        User user = new User();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDisplayName("new-user");
        request.setEmail("new@example.com");
        request.setName("New");
        request.setSurname("User");
        request.setAddress("New street");
        request.setPhone("999");

        userWebMapper.updateDomain(user, request);

        assertEquals("new-user", user.getDisplayName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("New", user.getName());
        assertEquals("User", user.getSurname());
        assertEquals("New street", user.getAddress());
        assertEquals("999", user.getPhone());
    }

    @Test
    void toResponseMapsUserToResponseIncludingRoleId() {
        Role role = new Role();
        role.setId(7L);

        User user = new User();
        user.setId(8L);
        user.setDisplayName("juan");
        user.setEmail("juan@example.com");
        user.setCreated(LocalDateTime.of(2026, 3, 21, 10, 0));
        user.setEnabled(true);
        user.setName("Juan");
        user.setSurname("Urban");
        user.setAddress("Street");
        user.setPhone("123");
        user.setRole(role);

        UserResponse response = userWebMapper.toResponse(user);

        assertEquals(8L, response.getId());
        assertEquals("juan", response.getDisplayName());
        assertEquals("juan@example.com", response.getEmail());
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
