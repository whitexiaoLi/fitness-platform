package com.fitness.service;

import com.fitness.entity.WorkoutPlan;
import java.util.List;

public interface WorkoutPlanService {
    List<WorkoutPlan> listByUser(Long userId);
    WorkoutPlan create(Long userId, WorkoutPlan plan);
    WorkoutPlan update(Long id, Long userId, WorkoutPlan plan);
    void delete(Long id, Long userId);
}
