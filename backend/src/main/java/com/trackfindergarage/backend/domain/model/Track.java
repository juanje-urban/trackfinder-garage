package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tracks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tracks_name", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "location", length = 255, nullable = false)
    private String location;

    @Column(name = "description", length = 500, nullable = false)
    private String description;
}