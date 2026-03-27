package com.trackfindergarage.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

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

    @Id
    @Column(name = "id_user", nullable = false, updatable = false)
    private Long idUser;

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

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.newEntity = false;
    }
}
