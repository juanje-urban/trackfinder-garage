package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Representa un tiempo de vuelta registrado por un usuario en un circuito concreto.
 */
@Entity
@Table(name = "lap_times")
@Getter
@Setter
@NoArgsConstructor
public class LapTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_track", nullable = false)
    private Track track;

    @Column(name = "lap_date", nullable = false)
    private LocalDate lapDate;

    @Column(name = "lap_time_ms", nullable = false)
    private Long lapTimeMs;

    @Column(name = "vehicle", length = 30)
    private String vehicle;
}
