package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.OperationLog;
import com.fitness.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    public ApiResponse<PageResult<OperationLog>> listLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<OperationLog> result = operationLogService.listLogs(page, size, keyword);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }
}
