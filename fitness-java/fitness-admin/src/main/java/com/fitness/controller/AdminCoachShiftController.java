package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.CoachShift;
import com.fitness.service.AdminCoachShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shifts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCoachShiftController {

    @Autowired
    private AdminCoachShiftService adminCoachShiftService;

    @GetMapping
    public ApiResponse<List<CoachShift>> getShifts(@RequestParam Long coachId) {
        return ApiResponse.success(adminCoachShiftService.getShifts(coachId));
    }

    @PostMapping
    public ApiResponse<List<CoachShift>> saveShifts(@RequestBody List<CoachShift> shifts) {
        if (shifts.isEmpty()) return ApiResponse.success(shifts);
        Long coachId = shifts.get(0).getCoachId();
        return ApiResponse.success(adminCoachShiftService.saveShifts(coachId, shifts));
    }
}
