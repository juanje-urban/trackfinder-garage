package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizerResponse {

    private Long idUser;
    private String displayName;
    private String email;
    private String name;
    private String surname;
    private String address;
    private String phone;
    private Boolean userEnabled;
    private Long roleId;

    //Atributos exclusivos de los organizadores
    private String legalName;
    private String cif;
    private Boolean organizerEnabled;
}