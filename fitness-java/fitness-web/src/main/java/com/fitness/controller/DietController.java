package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.DietRecord;
import com.fitness.security.SecurityUser;
import com.fitness.service.DietService;
import jakarta.validation.Valid;
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
                                              @Valid @RequestBody DietRecord record) {
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

    @PutMapping("/records/{id}")
    public ApiResponse<DietRecord> updateRecord(@AuthenticationPrincipal SecurityUser securityUser,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody DietRecord record) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(dietService.updateRecord(id, userId, record));
    }

    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(@AuthenticationPrincipal SecurityUser securityUser,
                                           @PathVariable Long id) {
        Long userId = securityUser.getUser().getId();
        dietService.deleteRecord(id, userId);
        return ApiResponse.success();
    }
}
