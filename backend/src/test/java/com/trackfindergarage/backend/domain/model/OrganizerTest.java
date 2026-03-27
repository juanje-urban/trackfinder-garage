package com.trackfindergarage.backend.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganizerTest {

    @Test
    void isNewReturnsTrueWhenOrganizerOnlyHasMappedUser() {
        User user = new User();
        user.setId(7L);

        Organizer organizer = new Organizer();
        organizer.setUser(user);

        assertTrue(organizer.isNew());
    }

    @Test
    void isNewReturnsFalseWhenSharedPrimaryKeyIsAlreadyAssigned() {
        Organizer organizer = new Organizer();

        organizer.setIdUser(7L);

        assertFalse(organizer.isNew());
    }

    @Test
    void markNotNewClearsTransientNewStateAfterPersistenceLifecycle() {
        User user = new User();
        user.setId(7L);

        Organizer organizer = new Organizer();
        organizer.setUser(user);

        organizer.markNotNew();

        assertFalse(organizer.isNew());
    }
}
