package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Track;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackPersistencePort trackPersistencePort;

    @InjectMocks
    private TrackService trackService;

    @Test
    void createTrackDelegatesSave() {
        Track track = trackWithId(1L, "Montmelo");

        when(trackPersistencePort.findByName("Montmelo")).thenReturn(Optional.empty());
        when(trackPersistencePort.findByShortName("montmelo")).thenReturn(Optional.empty());
        when(trackPersistencePort.save(track)).thenReturn(track);

        Track createdTrack = trackService.createTrack(track);

        assertSame(track, createdTrack);
        verify(trackPersistencePort).save(track);
    }

    @Test
    void updateTrackCopiesMutableFieldsAndSaves() {
        Track existingTrack = trackWithId(4L, "Old");
        existingTrack.setLocation("Barcelona");
        existingTrack.setDescription("Old description");

        Track updateRequest = trackWithId(null, "New");
        updateRequest.setShortName("new_track");
        updateRequest.setLocation("Valencia");
        updateRequest.setDescription("New description");

        when(trackPersistencePort.findById(4L)).thenReturn(Optional.of(existingTrack));
        when(trackPersistencePort.findByName("New")).thenReturn(Optional.empty());
        when(trackPersistencePort.findByShortName("new_track")).thenReturn(Optional.empty());
        when(trackPersistencePort.save(existingTrack)).thenReturn(existingTrack);

        Track updatedTrack = trackService.updateTrack(4L, updateRequest);

        assertSame(existingTrack, updatedTrack);
        assertEquals("New", existingTrack.getName());
        assertEquals("new_track", existingTrack.getShortName());
        assertEquals("Valencia", existingTrack.getLocation());
        assertEquals("New description", existingTrack.getDescription());
        verify(trackPersistencePort).save(existingTrack);
    }

    @Test
    void createTrackThrowsWhenDescriptionIsMissing() {
        Track track = trackWithId(1L, "Montmelo");
        track.setLocation("Barcelona");
        track.setDescription("   ");

        assertThrows(IllegalArgumentException.class, () -> trackService.createTrack(track));
        verifyNoInteractions(trackPersistencePort);
    }

    @Test
    void createTrackThrowsWhenNameAlreadyExists() {
        Track track = trackWithId(null, "Jarama");

        when(trackPersistencePort.findByName("Jarama")).thenReturn(Optional.of(trackWithId(1L, "Jarama")));

        assertThrows(DuplicateResourceException.class, () -> trackService.createTrack(track));

        verify(trackPersistencePort).findByName("Jarama");
        verifyNoMoreInteractions(trackPersistencePort);
    }

    @Test
    void createTrackThrowsWhenShortNameAlreadyExists() {
        Track track = trackWithId(null, "Jarama");
        track.setShortName("jarama");

        when(trackPersistencePort.findByName("Jarama")).thenReturn(Optional.empty());
        when(trackPersistencePort.findByShortName("jarama")).thenReturn(Optional.of(trackWithId(2L, "Other track")));

        assertThrows(DuplicateResourceException.class, () -> trackService.createTrack(track));

        verify(trackPersistencePort).findByName("Jarama");
        verify(trackPersistencePort).findByShortName("jarama");
        verifyNoMoreInteractions(trackPersistencePort);
    }

    @Test
    void updateTrackThrowsWhenShortNameBelongsToAnotherTrack() {
        Track existingTrack = trackWithId(4L, "Old");
        Track updateRequest = trackWithId(null, "New");
        updateRequest.setShortName("shared_short_name");

        when(trackPersistencePort.findById(4L)).thenReturn(Optional.of(existingTrack));
        when(trackPersistencePort.findByName("New")).thenReturn(Optional.empty());
        when(trackPersistencePort.findByShortName("shared_short_name"))
                .thenReturn(Optional.of(trackWithId(9L, "Another")));

        assertThrows(DuplicateResourceException.class, () -> trackService.updateTrack(4L, updateRequest));

        verify(trackPersistencePort).findById(4L);
        verify(trackPersistencePort).findByName("New");
        verify(trackPersistencePort).findByShortName("shared_short_name");
        verifyNoMoreInteractions(trackPersistencePort);
    }

    @Test
    void deleteTrackRemovesExistingTrack() {
        Track existingTrack = trackWithId(7L, "Jarama");

        when(trackPersistencePort.findById(7L)).thenReturn(Optional.of(existingTrack));

        trackService.deleteTrack(7L);

        verify(trackPersistencePort).delete(existingTrack);
    }

    @Test
    void getAllTracksReturnsPersistenceResult() {
        List<Track> tracks = List.of(trackWithId(1L, "Jarama"), trackWithId(2L, "Montmelo"));

        when(trackPersistencePort.findAll()).thenReturn(tracks);

        assertEquals(tracks, trackService.getAllTracks());
    }

    @Test
    void getTrackByIdThrowsWhenTrackDoesNotExist() {
        when(trackPersistencePort.findById(88L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> trackService.getTrackById(88L));
    }

    private Track trackWithId(Long id, String name) {
        Track track = new Track();
        track.setId(id);
        track.setName(name);
        track.setShortName(name.trim().toLowerCase().replace(' ', '_'));
        track.setLocation("Location");
        track.setDescription("Description");
        return track;
    }
}
