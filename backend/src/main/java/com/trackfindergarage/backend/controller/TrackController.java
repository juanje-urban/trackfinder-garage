package com.trackfindergarage.backend.controller;


import com.trackfindergarage.backend.entity.Track;
import com.trackfindergarage.backend.service.TrackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;
    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }
    @GetMapping
    public ResponseEntity<List<Track>> findAll() {
        return ResponseEntity.ok(trackService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Track> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(trackService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Track> save(@Valid @RequestBody Track track) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trackService.save(track));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Track> update(@PathVariable Integer id, @Valid @RequestBody Track track) {
        return ResponseEntity.ok(trackService.update(id, track));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        trackService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}
