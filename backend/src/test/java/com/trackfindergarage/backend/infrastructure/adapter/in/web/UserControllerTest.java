package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.PublicProfileUseCase;
import com.trackfindergarage.backend.application.port.in.PublicUserProfileView;
import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.PublicUserProfileResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateCurrentUserProfileRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.UserWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserUseCase userUseCase = mock(UserUseCase.class);
    private final PublicProfileUseCase publicProfileUseCase = mock(PublicProfileUseCase.class);
    private final UserWebMapper userWebMapper = new UserWebMapper();
    private final UserController userController = new UserController(userUseCase, publicProfileUseCase, userWebMapper);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void getCurrentUserReturnsMappedResponse() {
        when(authentication.getName()).thenReturn("driver@example.com");
        when(userUseCase.getCurrentUser("driver@example.com")).thenReturn(user(1L, "driver", true));

        UserResponse response = userController.getCurrentUser(authentication);

        assertEquals(1L, response.getId());
        assertEquals("driver", response.getDisplayName());
    }

    @Test
    void getPublicUserProfileMapsRecordToResponse() {
        when(publicProfileUseCase.getPublicUserProfile("driver"))
                .thenReturn(new PublicUserProfileView(1L, "driver", 2L, 3L, 4L, 1L));

        PublicUserProfileResponse response = userController.getPublicUserProfile("driver");

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getCompletedEvents());
        assertEquals(1L, response.getPoleCount());
    }

    @Test
    void updateCurrentUserDelegatesToUseCase() {
        UpdateCurrentUserProfileRequest request = new UpdateCurrentUserProfileRequest();
        request.setName("Laura");
        request.setSurname("Sanz");
        request.setEmail("driver@example.com");
        request.setAddress("Calle Box 27");
        request.setPhone("666555444");
        request.setPassword("secret");

        when(authentication.getName()).thenReturn("driver@example.com");
        when(userUseCase.updateCurrentUserProfile(
                "driver@example.com",
                "Laura",
                "Sanz",
                "driver@example.com",
                "Calle Box 27",
                "666555444",
                "secret"
        )).thenReturn(user(1L, "driver", true));

        UserResponse response = userController.updateCurrentUser(authentication, request);

        assertEquals(1L, response.getId());
        verify(userUseCase).updateCurrentUserProfile(
                "driver@example.com",
                "Laura",
                "Sanz",
                "driver@example.com",
                "Calle Box 27",
                "666555444",
                "secret"
        );
    }

    @Test
    void administrativeEndpointsMapUsers() {
        when(userUseCase.getAllUsers()).thenReturn(List.of(user(1L, "driver", true), user(2L, "other", false)));
        when(userUseCase.enableUser(2L)).thenReturn(user(2L, "other", true));
        when(userUseCase.disableUser(1L)).thenReturn(user(1L, "driver", false));

        List<UserResponse> allUsers = userController.getAllUsers();
        UserResponse enabledUser = userController.enableUser(2L);
        UserResponse disabledUser = userController.disableUser(1L);

        assertEquals(2, allUsers.size());
        assertEquals(true, enabledUser.getEnabled());
        assertEquals(false, disabledUser.getEnabled());
    }

    private User user(Long id, String displayName, boolean enabled) {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("USER");

        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName + "@example.com");
        user.setEnabled(enabled);
        user.setRole(role);
        return user;
    }
}
