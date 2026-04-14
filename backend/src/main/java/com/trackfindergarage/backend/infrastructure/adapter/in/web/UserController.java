package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.PublicProfileUseCase;
import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.PublicUserProfileResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateCurrentUserProfileRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractWebController {

    private final UserUseCase userUseCase;
    private final PublicProfileUseCase publicProfileUseCase;
    private final UserWebMapper userWebMapper;

    public UserController(UserUseCase userUseCase,
                          PublicProfileUseCase publicProfileUseCase,
                          UserWebMapper userWebMapper) {
        this.userUseCase = userUseCase;
        this.publicProfileUseCase = publicProfileUseCase;
        this.userWebMapper = userWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User userToCreate = userWebMapper.toDomain(request);
        return userWebMapper.toResponse(userUseCase.createUser(userToCreate, request.getPassword()));
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        return userWebMapper.toResponse(userUseCase.getCurrentUser(authenticatedEmail(authentication)));
    }

    @GetMapping("/public/{displayName}")
    public PublicUserProfileResponse getPublicUserProfile(@PathVariable String displayName) {
        var publicProfile = publicProfileUseCase.getPublicUserProfile(displayName);

        return PublicUserProfileResponse.builder()
                .id(publicProfile.id())
                .displayName(publicProfile.displayName())
                .completedEvents(publicProfile.completedEvents())
                .visitedCircuits(publicProfile.visitedCircuits())
                .topFiveLapTimes(publicProfile.topFiveLapTimes())
                .poleCount(publicProfile.poleCount())
                .build();
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(Authentication authentication,
                                          @Valid @RequestBody UpdateCurrentUserProfileRequest request) {
        return userWebMapper.toResponse(
                userUseCase.updateCurrentUserProfile(
                        authenticatedEmail(authentication),
                        request.getName(),
                        request.getSurname(),
                        request.getEmail(),
                        request.getAddress(),
                        request.getPhone(),
                        request.getPassword()
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        User userToUpdate = new User();
        userWebMapper.updateDomain(userToUpdate, request);
        return userWebMapper.toResponse(userUseCase.updateUser(id, userToUpdate));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userUseCase.deleteUser(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return mapResponses(userUseCase.getAllUsers(), userWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.getUserById(id));
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse enableUser(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.enableUser(id));
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse disableUser(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.disableUser(id));
    }
}
