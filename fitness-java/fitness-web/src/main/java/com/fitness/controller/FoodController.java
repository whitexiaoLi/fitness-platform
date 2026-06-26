package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.entity.FoodItem;
import com.fitness.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diet/foods")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/search")
    public ApiResponse<List<FoodItem>> search(@RequestParam String keyword,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(foodService.searchFoods(keyword, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<FoodItem> getById(@PathVariable Long id) {
        return ApiResponse.success(foodService.getById(id));
    }

    @PostMapping("/custom")
    public ApiResponse<FoodItem> createCustom(@RequestBody FoodItem foodItem) {
        return ApiResponse.success(foodService.createCustom(foodItem));
    }
}
