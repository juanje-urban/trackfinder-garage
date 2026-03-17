package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserWebMapper userWebMapper;

    public UserController(UserUseCase userUseCase, UserWebMapper userWebMapper) {
        this.userUseCase = userUseCase;
        this.userWebMapper = userWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User userToCreate = userWebMapper.toDomain(request);

        User createdUser = userUseCase.createUser(
                userToCreate,
                request.getPassword()
        );

        return userWebMapper.toResponse(createdUser);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        User userToUpdate = new User();
        userWebMapper.updateDomain(userToUpdate, request);

        User updatedUser = userUseCase.updateUser(
                id,
                userToUpdate
        );

        return userWebMapper.toResponse(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userUseCase.deleteUser(id);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userUseCase.getAllUsers()
                .stream()
                .map(userWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.getUserById(id));
    }

    @PatchMapping("/{id}/enable")
    public UserResponse enableUser(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.enableUser(id));
    }

    @PatchMapping("/{id}/disable")
    public UserResponse disableUser(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.disableUser(id));
    }
}