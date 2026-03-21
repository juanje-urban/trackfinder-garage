package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.UserWebMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserUseCase userUseCase = mock(UserUseCase.class);
    private final UserWebMapper userWebMapper = new UserWebMapper();
    private final UserController userController = new UserController(userUseCase, userWebMapper);

    @Test
    void createUserDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateUserRequest request = new CreateUserRequest();
        request.setDisplayName("juan");
        request.setPassword("secret");
        request.setEmail("juan@example.com");
        request.setName("Juan");
        request.setSurname("Urban");
        request.setAddress("Street");
        request.setPhone("123");

        when(userUseCase.createUser(any(User.class), eq("secret"))).thenReturn(userWithId(1L, "juan"));

        UserResponse response = userController.createUser(request);

        assertEquals(1L, response.getId());
        assertEquals("juan", response.getDisplayName());
        verify(userUseCase).createUser(any(User.class), eq("secret"));
    }

    @Test
    void updateUserDelegatesToUseCaseAndReturnsMappedResponse() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDisplayName("new-user");
        request.setEmail("new@example.com");
        request.setName("New");
        request.setSurname("User");
        request.setAddress("New street");
        request.setPhone("999");

        when(userUseCase.updateUser(eq(2L), any(User.class))).thenReturn(userWithId(2L, "new-user"));

        UserResponse response = userController.updateUser(2L, request);

        assertEquals(2L, response.getId());
        assertEquals("new-user", response.getDisplayName());
        verify(userUseCase).updateUser(eq(2L), any(User.class));
    }

    @Test
    void getAllUsersMapsUseCaseResult() {
        when(userUseCase.getAllUsers()).thenReturn(List.of(userWithId(1L, "juan")));

        List<UserResponse> response = userController.getAllUsers();

        assertEquals(1, response.size());
        assertEquals("juan", response.getFirst().getDisplayName());
    }

    @Test
    void getByIdAndEnableDisableReturnMappedResponses() {
        User user = userWithId(3L, "juan");
        User enabled = userWithId(3L, "juan");
        enabled.setEnabled(true);
        User disabled = userWithId(3L, "juan");
        disabled.setEnabled(false);

        when(userUseCase.getUserById(3L)).thenReturn(user);
        when(userUseCase.enableUser(3L)).thenReturn(enabled);
        when(userUseCase.disableUser(3L)).thenReturn(disabled);

        UserResponse byId = userController.getUserById(3L);
        UserResponse enabledResponse = userController.enableUser(3L);
        UserResponse disabledResponse = userController.disableUser(3L);

        assertEquals(3L, byId.getId());
        assertTrue(enabledResponse.getEnabled());
        assertEquals(Boolean.FALSE, disabledResponse.getEnabled());
    }

    @Test
    void deleteUserDelegatesToUseCase() {
        userController.deleteUser(4L);

        verify(userUseCase).deleteUser(4L);
    }

    private User userWithId(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName + "@example.com");
        user.setName("Name");
        user.setSurname("Surname");
        user.setAddress("Address");
        user.setPhone("123");
        user.setEnabled(true);
        return user;
    }
}
