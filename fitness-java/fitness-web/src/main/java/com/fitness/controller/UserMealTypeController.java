package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.UserMealType;
import com.fitness.security.SecurityUser;
import com.fitness.service.UserMealTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diet/meal-types")
public class UserMealTypeController {

    @Autowired
    private UserMealTypeService userMealTypeService;

    @GetMapping
    public ApiResponse<List<UserMealType>> list(@AuthenticationPrincipal SecurityUser securityUser) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(userMealTypeService.listByUser(userId));
    }

    @PostMapping
    public ApiResponse<UserMealType> create(@AuthenticationPrincipal SecurityUser securityUser,
                                             @RequestBody UserMealType mealType) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(userMealTypeService.create(userId, mealType));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserMealType> update(@AuthenticationPrincipal SecurityUser securityUser,
                                             @PathVariable Long id,
                                             @RequestBody UserMealType mealType) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(userMealTypeService.update(id, userId, mealType));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal SecurityUser securityUser,
                                     @PathVariable Long id) {
        Long userId = securityUser.getUser().getId();
        userMealTypeService.delete(id, userId);
        return ApiResponse.success();
    }
}
