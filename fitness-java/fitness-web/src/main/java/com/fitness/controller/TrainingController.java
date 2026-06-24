package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.TrainingRecord;
import com.fitness.security.SecurityUser;
import com.fitness.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @PostMapping("/records")
    public ApiResponse<TrainingRecord> addRecord(@AuthenticationPrincipal SecurityUser securityUser,
                                                   @RequestBody TrainingRecord record) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(trainingService.addRecord(userId, record));
    }

    @GetMapping("/records")
    public ApiResponse<PageResult<TrainingRecord>> listRecords(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        Long userId = securityUser.getUser().getId();
        Page<TrainingRecord> result = trainingService.listRecords(userId, page, size, start, end);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/records/{id}")
    public ApiResponse<TrainingRecord> getRecord(@PathVariable Long id) {
        return ApiResponse.success(trainingService.getRecord(id));
    }
}
