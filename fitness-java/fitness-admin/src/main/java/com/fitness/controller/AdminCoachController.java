package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.dto.response.UserVO;
import com.fitness.service.AdminCoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coaches")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCoachController {

    @Autowired
    private AdminCoachService adminCoachService;

    @GetMapping
    public ApiResponse<PageResult<UserVO>> listCoaches(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<UserVO> result = adminCoachService.listCoaches(page, size, keyword);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserVO> updateCoachProfile(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String bio = (String) body.get("bio");
        String certifications = (String) body.get("certifications");
        String specialties = (String) body.get("specialties");
        Integer experience = body.get("experience") != null ? ((Number) body.get("experience")).intValue() : null;
        BigDecimal hourlyRate = body.get("hourlyRate") != null ? new BigDecimal(body.get("hourlyRate").toString()) : null;
        UserVO vo = adminCoachService.updateCoachProfile(id, bio, certifications, specialties, experience, hourlyRate);
        return ApiResponse.success(vo);
    }
}
