package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "organizer_services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_organizer_services_organizer_service",
                        columnNames = {"id_organizer", "id_service"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OrganizerService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_organizer", referencedColumnName = "id_user", nullable = false)
    private Organizer organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false)
    private Service service;

    @Column(name = "enabled")
    private Boolean enabled;
}
