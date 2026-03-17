package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "organizers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "legal_name"),
                @UniqueConstraint(columnNames = "cif")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Organizer {

    @Id
    @Column(name = "id_user", nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id_user")
    private User user;

    @Column(name = "legal_name", length = 255)
    private String legalName;

    @Column(name = "cif", length = 255)
    private String cif;

    @Column(name = "enabled")
    private Boolean enabled;
}