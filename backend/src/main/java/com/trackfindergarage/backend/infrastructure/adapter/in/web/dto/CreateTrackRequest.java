package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTrackRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 120)
    @Pattern(regexp = "^[a-z0-9_]+$")
    private String shortName;

    @NotBlank
    @Size(max = 255)
    private String location;

    @NotBlank
    @Size(max = 500)
    private String description;
}
