package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolePersistenceAdapterTest {

    private final SpringDataRoleRepository repository = mock(SpringDataRoleRepository.class);
    private final RolePersistenceAdapter adapter = new RolePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        Role role = new Role();
        when(repository.save(role)).thenReturn(role);

        Role saved = adapter.save(role);

        assertSame(role, saved);
        verify(repository).save(role);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Role role = new Role();
        List<Role> roles = List.of(role);

        when(repository.findById(1L)).thenReturn(Optional.of(role));
        when(repository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(repository.findAll()).thenReturn(roles);

        assertTrue(adapter.findById(1L).isPresent());
        assertTrue(adapter.findByRoleName("USER").isPresent());
        assertEquals(roles, adapter.findAll());
    }

    @Test
    void deleteDelegatesToRepository() {
        Role role = new Role();

        adapter.delete(role);

        verify(repository).delete(role);
    }
}
