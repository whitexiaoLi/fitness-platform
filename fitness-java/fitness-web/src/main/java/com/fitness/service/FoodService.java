package com.fitness.service;

import com.fitness.entity.FoodItem;
import java.util.List;

public interface FoodService {
    List<FoodItem> searchFoods(String keyword, int pageSize);
    FoodItem getById(Long id);
    FoodItem createCustom(FoodItem foodItem);
}
