package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.WorkoutPlan;
import com.fitness.security.SecurityUser;
import com.fitness.service.WorkoutPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/training/plans")
public class WorkoutPlanController {

    @Autowired
    private WorkoutPlanService workoutPlanService;

    @GetMapping
    public ApiResponse<List<WorkoutPlan>> list(@AuthenticationPrincipal SecurityUser securityUser) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(workoutPlanService.listByUser(userId));
    }

    @PostMapping
    public ApiResponse<WorkoutPlan> create(@AuthenticationPrincipal SecurityUser securityUser,
                                            @RequestBody WorkoutPlan plan) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(workoutPlanService.create(userId, plan));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkoutPlan> update(@AuthenticationPrincipal SecurityUser securityUser,
                                            @PathVariable Long id, @RequestBody WorkoutPlan plan) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(workoutPlanService.update(id, userId, plan));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal SecurityUser securityUser,
                                     @PathVariable Long id) {
        Long userId = securityUser.getUser().getId();
        workoutPlanService.delete(id, userId);
        return ApiResponse.success();
    }
}
