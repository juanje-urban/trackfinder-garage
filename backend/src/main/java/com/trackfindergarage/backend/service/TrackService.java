package com.trackfindergarage.backend.service;

import com.trackfindergarage.backend.entity.Track;
import com.trackfindergarage.backend.repository.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackService {
    private final TrackRepository trackRepository;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public List<Track> findAll() {
        return trackRepository.findAll();
    }

    public Track findById(Integer id) {
        return trackRepository.findById(id).orElseThrow(() ->new RuntimeException("Track not found with id " + id));
    }

    public Track save(Track track) {
        return trackRepository.save(track);
    }

    public Track update(Integer id, Track track) {
        Track existingTrack = trackRepository.findById(id).orElseThrow(()-> new RuntimeException("Track not found with id " + id));

        existingTrack.setName(track.getName());
        existingTrack.setDescription(track.getDescription());
        existingTrack.setLocation(track.getLocation());

        return trackRepository.save(existingTrack);
    }

    public void deleteById(Integer id) {

        if(!trackRepository.existsById(id)) {
            throw new RuntimeException("Track not found with id " + id);
        }
        trackRepository.deleteById(id);
    }


}
