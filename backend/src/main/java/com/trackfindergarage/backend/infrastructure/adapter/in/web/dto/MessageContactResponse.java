package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageContactResponse {

    private Long id;
    private String displayName;
    private String roleName;
}
