package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
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
        updateRequest.setLocation("Valencia");
        updateRequest.setDescription("New description");

        when(trackPersistencePort.findById(4L)).thenReturn(Optional.of(existingTrack));
        when(trackPersistencePort.save(existingTrack)).thenReturn(existingTrack);

        Track updatedTrack = trackService.updateTrack(4L, updateRequest);

        assertSame(existingTrack, updatedTrack);
        assertEquals("New", existingTrack.getName());
        assertEquals("Valencia", existingTrack.getLocation());
        assertEquals("New description", existingTrack.getDescription());
        verify(trackPersistencePort).save(existingTrack);
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
        return track;
    }
}
