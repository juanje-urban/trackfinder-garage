package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPersistenceAdapterTest {

    private final SpringDataUserRepository repository = mock(SpringDataUserRepository.class);
    private final UserPersistenceAdapter adapter = new UserPersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        User user = new User();
        when(repository.save(user)).thenReturn(user);

        User saved = adapter.save(user);

        assertSame(user, saved);
        verify(repository).save(user);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        User user = new User();
        List<User> users = List.of(user);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByDisplayName("juan")).thenReturn(Optional.of(user));
        when(repository.findByEmail("juan@example.com")).thenReturn(Optional.of(user));
        when(repository.findAll()).thenReturn(users);

        assertTrue(adapter.findById(1L).isPresent());
        assertTrue(adapter.findByDisplayName("juan").isPresent());
        assertTrue(adapter.findByEmail("juan@example.com").isPresent());
        assertEquals(users, adapter.findAll());
    }

    @Test
    void deleteDelegatesToRepository() {
        User user = new User();

        adapter.delete(user);

        verify(repository).delete(user);
    }
}
