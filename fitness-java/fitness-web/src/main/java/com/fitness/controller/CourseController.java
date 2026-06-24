package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.CourseVO;
import com.fitness.dto.response.PageResult;
import com.fitness.enums.CourseCategory;
import com.fitness.enums.DifficultyLevel;
import com.fitness.security.SecurityUser;
import com.fitness.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/courses")
    public ApiResponse<PageResult<CourseVO>> listCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) DifficultyLevel difficulty) {
        Page<CourseVO> result = courseService.listCourses(page, size, keyword, category, difficulty);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<CourseVO> getCourse(@PathVariable Long id) {
        return ApiResponse.success(courseService.getCourse(id));
    }

    @PostMapping("/courses/{id}/subscribe")
    public ApiResponse<Void> subscribe(@AuthenticationPrincipal SecurityUser securityUser,
                                        @PathVariable Long id) {
        Long userId = securityUser.getUser().getId();
        courseService.subscribe(userId, id);
        return ApiResponse.success();
    }

    @GetMapping("/user/courses")
    public ApiResponse<PageResult<CourseVO>> listMyCourses(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityUser.getUser().getId();
        Page<CourseVO> result = courseService.listMyCourses(userId, page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }
}
