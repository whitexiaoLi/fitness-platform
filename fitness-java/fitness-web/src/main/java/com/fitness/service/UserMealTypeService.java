package com.fitness.service;

import com.fitness.entity.UserMealType;
import java.util.List;

public interface UserMealTypeService {
    List<UserMealType> listByUser(Long userId);
    UserMealType create(Long userId, UserMealType mealType);
    UserMealType update(Long id, Long userId, UserMealType mealType);
    void delete(Long id, Long userId);
}
