package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.CoachPerformanceVO;
import com.fitness.service.CoachPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coach-performance")
@PreAuthorize("hasRole('ADMIN')")
public class CoachPerformanceController {

    @Autowired
    private CoachPerformanceService coachPerformanceService;

    @GetMapping
    public ApiResponse<List<CoachPerformanceVO>> getPerformance() {
        return ApiResponse.success(coachPerformanceService.getPerformance());
    }
}
