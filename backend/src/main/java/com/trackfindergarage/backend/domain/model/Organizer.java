package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

/**
 * Representa la información específica de una cuenta organizadora.
 *
 * <p>Un organizador se apoya en una relación uno a uno con {@link User} y reutiliza su misma clave primaria. De esta
 * manera, la identidad común del usuario y los datos propios de la entidad organizadora permanecen separados pero
 * vinculados. Similar a una extensión pero sin serlo.</p>
 */
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
public class Organizer implements Persistable<Long> {

    /**
     * Identificador del organizador, compartido con el usuario asociado.
     */
    @Id
    @Column(name = "id_user", nullable = false, updatable = false)
    private Long idUser;

    /**
     * Usuario vinculado al que pertenece esta información de organizador.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id_user")
    private User user;

    /**
     * Razón social o denominación legal del organizador.
     */
    @Column(name = "legal_name", length = 255)
    private String legalName;

    /**
     * Identificador fiscal del organizador.
     */
    @Column(name = "cif", length = 255)
    private String cif;

    /**
     * Indica si la solicitud o cuenta de organizador está habilitada. La 'parte' de User se considera habilitada, pero
     * la parte de Organizer debe ser aprobada por un administrador.
     */
    @Column(name = "enabled")
    private Boolean enabled;

    /**
     * Marca transitoria usada por Spring Data para distinguir altas de actualizaciones. Lo necesitamos porque cuando
     * creamos un Organizer, previamente hemos creado un User (el id de Organizer es el id de User). Por lo que como no
     * tenemos un id null, no sabemos si el Organizer es nuevo o no.
     */
    @Transient
    private boolean newEntity = true;

    @Override
    public Long getId() {
        return idUser;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
        this.newEntity = idUser == null;
    }

    /**
     * Sincroniza el estado interno para que una entidad cargada o persistida deje de considerarse nueva.
     */
    @PostPersist
    @PostLoad
    void markNotNew() {
        this.newEntity = false;
    }
}
