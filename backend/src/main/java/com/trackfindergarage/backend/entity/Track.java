package com.trackfindergarage.backend.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Column(name = "location", length = 255, nullable = false)
    private String location;

    @NotBlank
    @Size(max = 500)
    @Column(name = "description", length = 500, nullable = false)
    private String description;




}
