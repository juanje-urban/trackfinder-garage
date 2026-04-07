package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private Long userId;
    private String displayName;
    private String email;
    private String roleName;
    private String authorizationHeader;
}
