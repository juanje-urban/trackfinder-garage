package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByDisplayName(String displayName);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void delete(User user);
}