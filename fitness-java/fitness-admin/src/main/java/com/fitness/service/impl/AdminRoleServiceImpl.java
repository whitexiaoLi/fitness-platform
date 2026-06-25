package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.Permission;
import com.fitness.entity.Role;
import com.fitness.entity.RolePermission;
import com.fitness.mapper.PermissionMapper;
import com.fitness.mapper.RoleMapper;
import com.fitness.mapper.RolePermissionMapper;
import com.fitness.service.AdminRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Role> listRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public List<Permission> listPermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getGroupName));
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId))
                .stream().map(RolePermission::getPermissionId).toList();
    }

    @Override
    @Transactional
    public void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        for (Long permId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }
}
