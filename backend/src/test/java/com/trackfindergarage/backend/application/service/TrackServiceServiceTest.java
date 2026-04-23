package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackServiceServiceTest {

    @Mock
    private TrackServicePersistencePort trackServicePersistencePort;

    @Mock
    private TrackPersistencePort trackPersistencePort;

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @InjectMocks
    private TrackServiceService trackServiceService;

    @Test
    void createTrackServicePersistsWhenServiceIsAllowedForTrack() {
        TrackService trackService = trackServiceWithIds(1L, 2L);
        Track track = trackWithId(1L);
        com.trackfindergarage.backend.domain.model.Service service = serviceWithId(2L);
        service.setAllowedForTrack(true);

        when(trackServicePersistencePort.findByTrackIdAndServiceId(1L, 2L)).thenReturn(Optional.empty());
        when(trackPersistencePort.findById(1L)).thenReturn(Optional.of(track));
        when(servicePersistencePort.findById(2L)).thenReturn(Optional.of(service));
        when(trackServicePersistencePort.save(trackService)).thenReturn(trackService);

        TrackService created = trackServiceService.createTrackService(trackService);

        assertSame(trackService, created);
        assertSame(track, trackService.getTrack());
        assertSame(service, trackService.getService());
        verify(trackServicePersistencePort).save(trackService);
    }

    @Test
    void createTrackServiceThrowsWhenDuplicateExists() {
        TrackService trackService = trackServiceWithIds(1L, 2L);

        when(trackServicePersistencePort.findByTrackIdAndServiceId(1L, 2L)).thenReturn(Optional.of(trackService));

        assertThrows(DuplicateResourceException.class, () -> trackServiceService.createTrackService(trackService));
        verify(trackServicePersistencePort, never()).save(trackService);
    }

    @Test
    void createTrackServiceThrowsWhenServiceIsNotAllowedForTrack() {
        TrackService trackService = trackServiceWithIds(1L, 2L);
        Track track = trackWithId(1L);
        com.trackfindergarage.backend.domain.model.Service service = serviceWithId(2L);
        service.setAllowedForTrack(false);

        when(trackServicePersistencePort.findByTrackIdAndServiceId(1L, 2L)).thenReturn(Optional.empty());
        when(trackPersistencePort.findById(1L)).thenReturn(Optional.of(track));
        when(servicePersistencePort.findById(2L)).thenReturn(Optional.of(service));

        assertThrows(IllegalArgumentException.class, () -> trackServiceService.createTrackService(trackService));
        verify(trackServicePersistencePort, never()).save(trackService);
    }

    @Test
    void deleteTrackServiceRemovesExistingAssignment() {
        TrackService trackService = trackServiceWithIds(1L, 2L);
        trackService.setId(3L);

        when(trackServicePersistencePort.findById(3L)).thenReturn(Optional.of(trackService));

        trackServiceService.deleteTrackService(3L);

        verify(trackServicePersistencePort).delete(trackService);
    }

    @Test
    void getAllTrackServicesReturnsPersistenceResult() {
        List<TrackService> assignments = List.of(trackServiceWithIds(1L, 2L));

        when(trackServicePersistencePort.findAll()).thenReturn(assignments);

        assertEquals(assignments, trackServiceService.getAllTrackServices());
    }

    private TrackService trackServiceWithIds(Long trackId, Long serviceId) {
        Track track = new Track();
        track.setId(trackId);

        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(serviceId);

        TrackService trackService = new TrackService();
        trackService.setTrack(track);
        trackService.setService(service);
        return trackService;
    }

    private Track trackWithId(Long id) {
        Track track = new Track();
        track.setId(id);
        track.setName("Track");
        return track;
    }

    private com.trackfindergarage.backend.domain.model.Service serviceWithId(Long id) {
        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(id);
        service.setName("Service");
        return service;
    }
}
