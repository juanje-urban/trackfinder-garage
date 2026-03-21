package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "services",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_services_name", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor


public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /*Los siguientes dos campos nos sirven para identificar si un servicio puede ser ofrecido por la pista,
     el organizador o ambos*/

    @Column(name = "allowed_for_track")
    private Boolean allowedForTrack;

    @Column(name = "allowed_for_organizer")
    private Boolean allowedForOrganizer;

    @Column(name = "enabled")
    private Boolean enabled;
}
