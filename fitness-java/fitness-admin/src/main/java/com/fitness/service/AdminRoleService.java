package com.fitness.service;

import com.fitness.entity.Permission;
import com.fitness.entity.Role;
import java.util.List;

public interface AdminRoleService {
    List<Role> listRoles();
    List<Permission> listPermissions();
    List<Long> getRolePermissionIds(Long roleId);
    void saveRolePermissions(Long roleId, List<Long> permissionIds);
}
