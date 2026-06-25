package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.CoachStudent;
import com.fitness.service.AdminCoachStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/coach-students")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCoachStudentController {

    @Autowired
    private AdminCoachStudentService adminCoachStudentService;

    @GetMapping
    public ApiResponse<PageResult<CoachStudent>> listBindings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long coachId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status) {
        Page<CoachStudent> result = adminCoachStudentService.listBindings(page, size, coachId, studentId, status);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping
    public ApiResponse<CoachStudent> createBinding(@RequestBody Map<String, Long> body) {
        return ApiResponse.success(adminCoachStudentService.createBinding(body.get("coachId"), body.get("studentId")));
    }

    @PutMapping("/{id}/end")
    public ApiResponse<Void> endBinding(@PathVariable Long id) {
        adminCoachStudentService.endBinding(id);
        return ApiResponse.success();
    }
}
