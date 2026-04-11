package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.User;

import java.util.List;

public interface UserUseCase {

    User createUser(User user, String rawPassword);

    User getCurrentUser(String authenticatedEmail);

    User updateCurrentUserProfile(String authenticatedEmail,
                                  String name,
                                  String surname,
                                  String email,
                                  String address,
                                  String phone,
                                  String rawPassword);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    List<User> getAllUsers();

    User getUserById(Long id);

    User enableUser(Long id);

    User disableUser(Long id);
}
