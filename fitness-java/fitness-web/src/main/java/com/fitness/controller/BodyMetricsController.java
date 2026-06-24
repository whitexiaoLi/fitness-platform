package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.BodyMetrics;
import com.fitness.security.SecurityUser;
import com.fitness.service.BodyMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class BodyMetricsController {

    @Autowired
    private BodyMetricsService bodyMetricsService;

    @PostMapping
    public ApiResponse<BodyMetrics> addRecord(@AuthenticationPrincipal SecurityUser securityUser,
                                                @RequestBody BodyMetrics record) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(bodyMetricsService.addRecord(userId, record));
    }

    @GetMapping
    public ApiResponse<List<BodyMetrics>> listRecords(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(bodyMetricsService.listRecords(userId, start, end));
    }

    @GetMapping("/latest")
    public ApiResponse<BodyMetrics> getLatest(@AuthenticationPrincipal SecurityUser securityUser) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(bodyMetricsService.getLatest(userId));
    }
}
