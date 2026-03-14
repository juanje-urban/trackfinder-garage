package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse {

    private Long id;
    private String name;
}