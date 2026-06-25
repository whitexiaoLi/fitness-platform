package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.BodyMetrics;
import com.fitness.service.AdminMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/metrics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMetricsController {

    @Autowired
    private AdminMetricsService adminMetricsService;

    @GetMapping
    public ApiResponse<PageResult<BodyMetrics>> listMetrics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        Page<BodyMetrics> result = adminMetricsService.listMetrics(page, size, userId);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }
}
