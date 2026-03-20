package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrganizerRequest {

    @NotBlank
    @Size(max = 255)
    private String displayName;

    @NotBlank
    @Size(max = 255)
    private String password;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String surname;

    @NotBlank
    @Size(max = 255)
    private String address;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 255)
    private String legalName;

    @NotBlank
    @Size(max = 20)
    private String cif;
}