package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.*;
import com.fitness.security.SecurityUser;
import com.fitness.service.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coach")
@PreAuthorize("hasRole('COACH')")
public class CoachController {

    @Autowired
    private CoachService coachService;

    // === Course Management ===
    @PostMapping("/courses")
    public ApiResponse<Course> createCourse(@AuthenticationPrincipal SecurityUser securityUser,
                                              @RequestBody Course course) {
        return ApiResponse.success(coachService.createCourse(securityUser.getUser().getId(), course));
    }

    @PutMapping("/courses/{id}")
    public ApiResponse<Course> updateCourse(@AuthenticationPrincipal SecurityUser securityUser,
                                              @PathVariable Long id, @RequestBody Course course) {
        return ApiResponse.success(coachService.updateCourse(securityUser.getUser().getId(), id, course));
    }

    @GetMapping("/courses")
    public ApiResponse<PageResult<Course>> listMyCourses(@AuthenticationPrincipal SecurityUser securityUser,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Page<Course> result = coachService.listMyCourses(securityUser.getUser().getId(), page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> deleteCourse(@AuthenticationPrincipal SecurityUser securityUser,
                                           @PathVariable Long id) {
        coachService.deleteCourse(securityUser.getUser().getId(), id);
        return ApiResponse.success();
    }

    // === Student Management ===
    @GetMapping("/students")
    public ApiResponse<PageResult<UserVO>> listStudents(@AuthenticationPrincipal SecurityUser securityUser,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Page<UserVO> result = coachService.listStudents(securityUser.getUser().getId(), page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/students/{userId}/training")
    public ApiResponse<PageResult<TrainingRecord>> getStudentTraining(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TrainingRecord> result = coachService.getStudentTraining(userId, page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/students/{userId}/metrics")
    public ApiResponse<PageResult<BodyMetrics>> getStudentMetrics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BodyMetrics> result = coachService.getStudentMetrics(userId, page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    // === Training Plans ===
    @PostMapping("/plans")
    public ApiResponse<TrainingPlan> createPlan(@AuthenticationPrincipal SecurityUser securityUser,
                                                  @RequestBody TrainingPlan plan) {
        return ApiResponse.success(coachService.createPlan(securityUser.getUser().getId(), plan));
    }

    @GetMapping("/plans")
    public ApiResponse<PageResult<TrainingPlan>> listMyPlans(@AuthenticationPrincipal SecurityUser securityUser,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Page<TrainingPlan> result = coachService.listMyPlans(securityUser.getUser().getId(), page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/plans/me")
    public ApiResponse<PageResult<TrainingPlan>> listUserPlans(@AuthenticationPrincipal SecurityUser securityUser,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        Page<TrainingPlan> result = coachService.listUserPlans(securityUser.getUser().getId(), page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }
}
