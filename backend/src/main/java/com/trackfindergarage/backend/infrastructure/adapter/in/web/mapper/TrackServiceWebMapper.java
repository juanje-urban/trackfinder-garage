package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class TrackServiceWebMapper {

    public TrackService toDomain(CreateTrackServiceRequest request) {
        Track track = new Track();
        track.setId(request.getTrackId());

        Service service = new Service();
        service.setId(request.getServiceId());

        TrackService trackService = new TrackService();
        trackService.setTrack(track);
        trackService.setService(service);

        return trackService;
    }

    public TrackServiceResponse toResponse(TrackService trackService) {
        return TrackServiceResponse.builder()
                .id(trackService.getId())
                .trackId(trackService.getTrack() != null ? trackService.getTrack().getId() : null)
                .trackName(trackService.getTrack() != null ? trackService.getTrack().getName() : null)
                .serviceId(trackService.getService() != null ? trackService.getService().getId() : null)
                .serviceName(trackService.getService() != null ? trackService.getService().getName() : null)
                .build();
    }
}
