package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.DietRecord;
import com.fitness.security.SecurityUser;
import com.fitness.service.DietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/diet")
public class DietController {

    @Autowired
    private DietService dietService;

    @PostMapping("/records")
    public ApiResponse<DietRecord> addRecord(@AuthenticationPrincipal SecurityUser securityUser,
                                               @RequestBody DietRecord record) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(dietService.addRecord(userId, record));
    }

    @GetMapping("/records")
    public ApiResponse<Map<String, Object>> listByDate(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam LocalDate date) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(dietService.listByDate(userId, date));
    }

    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        dietService.deleteRecord(id);
        return ApiResponse.success();
    }
}
