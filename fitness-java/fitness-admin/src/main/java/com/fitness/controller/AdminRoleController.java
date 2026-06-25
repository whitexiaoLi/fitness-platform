package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.Permission;
import com.fitness.entity.Role;
import com.fitness.service.AdminRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    @Autowired
    private AdminRoleService adminRoleService;

    @GetMapping
    public ApiResponse<List<Role>> listRoles() {
        return ApiResponse.success(adminRoleService.listRoles());
    }

    @GetMapping("/permissions")
    public ApiResponse<List<Permission>> listPermissions() {
        return ApiResponse.success(adminRoleService.listPermissions());
    }

    @GetMapping("/{roleId}/permissions")
    public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return ApiResponse.success(adminRoleService.getRolePermissionIds(roleId));
    }

    @PutMapping("/{roleId}/permissions")
    public ApiResponse<Void> saveRolePermissions(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        adminRoleService.saveRolePermissions(roleId, body.get("permissionIds"));
        return ApiResponse.success();
    }
}
