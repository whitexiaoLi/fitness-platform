package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.Course;
import com.fitness.service.AdminCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    @Autowired
    private AdminCourseService adminCourseService;

    @GetMapping
    public ApiResponse<PageResult<Course>> listCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Page<Course> result = adminCourseService.listCourses(page, size, status);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approveCourse(@PathVariable Long id) {
        adminCourseService.approveCourse(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Void> rejectCourse(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminCourseService.rejectCourse(id, body.get("reason"));
        return ApiResponse.success();
    }
}
