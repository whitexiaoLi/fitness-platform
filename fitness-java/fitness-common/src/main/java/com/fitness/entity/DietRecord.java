package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_record")
public class DietRecord extends BaseEntity {
    private Long userId;

    @NotBlank(message = "餐型不能为空")
    private String mealType;

    private Long foodId;
    private BigDecimal weightGrams;

    @NotBlank(message = "食物名称不能为空")
    private String foodName;

    @Min(value = 0, message = "热量不能为负数")
    private Integer calories;

    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private String imageUrl;
    private LocalDate recordDate;
}
