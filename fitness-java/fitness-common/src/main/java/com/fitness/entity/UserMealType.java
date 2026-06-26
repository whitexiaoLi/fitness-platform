package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_meal_type")
public class UserMealType extends BaseEntity {
    private Long userId;
    private String name;
    private String code;
    private Integer sortOrder;
    private Integer isActive;
}
