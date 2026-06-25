package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.GroupClass;
import com.fitness.service.AdminGroupClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/group-classes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGroupClassController {

    @Autowired
    private AdminGroupClassService adminGroupClassService;

    @GetMapping
    public ApiResponse<PageResult<GroupClass>> listClasses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Page<GroupClass> result = adminGroupClassService.listClasses(page, size, status);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping
    public ApiResponse<GroupClass> createClass(@RequestBody GroupClass groupClass) {
        return ApiResponse.success(adminGroupClassService.createClass(groupClass));
    }

    @PutMapping("/{id}")
    public ApiResponse<GroupClass> updateClass(@PathVariable Long id, @RequestBody GroupClass groupClass) {
        groupClass.setId(id);
        return ApiResponse.success(adminGroupClassService.updateClass(groupClass));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancelClass(@PathVariable Long id) {
        adminGroupClassService.cancelClass(id);
        return ApiResponse.success();
    }
}
