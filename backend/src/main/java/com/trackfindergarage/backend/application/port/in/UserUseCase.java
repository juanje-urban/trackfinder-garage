package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.User;

import java.util.List;

public interface UserUseCase {

    User createUser(User user, String rawPassword, Long roleId);

    User updateUser(Long id, User user, Long roleId);

    void deleteUser(Long id);

    List<User> getAllUsers();

    User getUserById(Long id);

    User enableUser(Long id);

    User disableUser(Long id);
}