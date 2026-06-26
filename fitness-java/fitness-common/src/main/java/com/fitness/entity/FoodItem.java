package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("food_item")
public class FoodItem extends BaseEntity {
    private Integer fdcId;
    private String name;
    private String nameEn;
    private String category;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private String source;
    private Integer isCommon;
}
