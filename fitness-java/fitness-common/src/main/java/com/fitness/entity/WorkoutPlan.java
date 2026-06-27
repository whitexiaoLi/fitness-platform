package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workout_plan")
public class WorkoutPlan extends BaseEntity {
    private Long userId;
    private String name;
    private String trainingType;
    private String exercises;  // JSON: [{exerciseId, title, sets, restSeconds}]
}
