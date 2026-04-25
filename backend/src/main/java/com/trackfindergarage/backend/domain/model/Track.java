package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un circuito disponible en el catalogo de la aplicación.
 *
 * <p>Guarda la información básica con la que se publican los circuitos, se asocian servicios de pista
 * y se programan eventos.</p>
 */
@Entity
@Table(
        name = "tracks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tracks_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_tracks_short_name", columnNames = "short_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Track {

    /**
     * Id del circuito.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Nombre del circuito.
     */
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    /**
     * Nombre corto usado como alias funcional en la aplicación. Se usa para buscar los assets.
     */
    @Column(name = "short_name", length = 120, nullable = false)
    private String shortName;

    /**
     * Ubicación del circuito.
     */
    @Column(name = "location", length = 255, nullable = false)
    private String location;

    /**
     * Descripción.
     */
    @Column(name = "description", length = 500, nullable = false)
    private String description;
}
