package com.fitness.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.CourseExercise;
import com.fitness.entity.TrainingRecord;
import com.fitness.mapper.CourseExerciseMapper;
import com.fitness.security.SecurityUser;
import com.fitness.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private CourseExerciseMapper courseExerciseMapper;

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

    @PutMapping("/records/{id}")
    public ApiResponse<TrainingRecord> updateRecord(@AuthenticationPrincipal SecurityUser securityUser,
                                                      @PathVariable Long id,
                                                      @RequestBody TrainingRecord record) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(trainingService.updateRecord(id, userId, record));
    }

    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(@AuthenticationPrincipal SecurityUser securityUser,
                                           @PathVariable Long id) {
        Long userId = securityUser.getUser().getId();
        trainingService.deleteRecord(id, userId);
        return ApiResponse.success();
    }

    @GetMapping("/records/stats")
    public ApiResponse<Map<String, Object>> getStats(@AuthenticationPrincipal SecurityUser securityUser,
                                                       @RequestParam(required = false) LocalDate start,
                                                       @RequestParam(required = false) LocalDate end) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(trainingService.getStats(userId, start, end));
    }

    @GetMapping("/exercises")
    public ApiResponse<List<CourseExercise>> searchExercises(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CourseExercise> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String[] words = keyword.trim().split("\\s+");
            wrapper.and(w -> {
                for (int i = 0; i < words.length; i++) {
                    if (i == 0) w.like(CourseExercise::getTitle, words[i]);
                    else w.or().like(CourseExercise::getTitle, words[i]);
                }
            });
        }
        wrapper.orderByAsc(CourseExercise::getSortOrder).last("LIMIT 30");
        return ApiResponse.success(courseExerciseMapper.selectList(wrapper));
    }
}
