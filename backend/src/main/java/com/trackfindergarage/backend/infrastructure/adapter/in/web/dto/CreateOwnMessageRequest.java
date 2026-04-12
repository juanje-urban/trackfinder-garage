package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOwnMessageRequest {

    @NotNull
    private Long receiverId;

    @NotBlank
    @Size(max = 255)
    private String subject;

    @NotBlank
    @Size(max = 500)
    private String message;
}
