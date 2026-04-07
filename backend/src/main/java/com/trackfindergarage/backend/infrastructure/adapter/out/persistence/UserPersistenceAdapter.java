package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserPersistencePort {

    private final SpringDataUserRepository springDataUserRepository;

    public UserPersistenceAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByDisplayName(String displayName) {
        return springDataUserRepository.findByDisplayName(displayName);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return springDataUserRepository.findByPhone(phone);
    }

    @Override
    public List<User> findAll() {
        return springDataUserRepository.findAll();
    }

    @Override
    public void delete(User user) {
        springDataUserRepository.delete(user);
    }
}
