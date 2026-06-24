package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.dto.response.UserVO;
import com.fitness.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResult<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        Page<UserVO> result = adminUserService.listUsers(page, size, role, status);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserDetail(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.getUserDetail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminUserService.updateUserStatus(id, body.get("status"));
        return ApiResponse.success();
    }
}
