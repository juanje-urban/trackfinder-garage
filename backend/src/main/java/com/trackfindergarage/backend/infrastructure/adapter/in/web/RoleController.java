package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.RoleUseCase;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateRoleRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.RoleResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateRoleRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.RoleWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController extends AbstractWebController {

    private final RoleUseCase roleUseCase;
    private final RoleWebMapper roleWebMapper;

    public RoleController(RoleUseCase roleUseCase, RoleWebMapper roleWebMapper) {
        this.roleUseCase = roleUseCase;
        this.roleWebMapper = roleWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        return roleWebMapper.toResponse(roleUseCase.createRole(roleWebMapper.toDomain(request)));
    }

    @PutMapping("/{id}")
    public RoleResponse updateRole(@PathVariable Long id,
                                   @Valid @RequestBody UpdateRoleRequest request) {
        Role roleToUpdate = new Role();
        roleWebMapper.updateDomain(roleToUpdate, request);
        return roleWebMapper.toResponse(roleUseCase.updateRole(id, roleToUpdate));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long id) {
        roleUseCase.deleteRole(id);
    }

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return mapResponses(roleUseCase.getAllRoles(), roleWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    public RoleResponse getRoleById(@PathVariable Long id) {
        return roleWebMapper.toResponse(roleUseCase.getRoleById(id));
    }
}
