package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.enums.MealType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_record")
public class DietRecord extends BaseEntity {
    private Long userId;
    private MealType mealType;
    private String foodName;
    private Integer calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private String imageUrl;
    private LocalDate recordDate;
}
