package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RolePersistenceAdapterTest {

    private final SpringDataRoleRepository repository = mock(SpringDataRoleRepository.class);
    private final RolePersistenceAdapter adapter = new RolePersistenceAdapter(repository);

    @Test
    void findByRoleNameDelegatesToRepository() {
        Role role = new Role();
        when(repository.findByRoleName("USER")).thenReturn(Optional.of(role));
        assertTrue(adapter.findByRoleName("USER").isPresent());
    }
}
