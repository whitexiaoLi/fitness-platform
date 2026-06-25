package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.UserCourse;
import com.fitness.service.AdminBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    @Autowired
    private AdminBookingService adminBookingService;

    @GetMapping
    public ApiResponse<PageResult<UserCourse>> listBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserCourse> result = adminBookingService.listBookings(page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }
}
