package com.fitness.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum MealType implements IEnum<String> {
    BREAKFAST("BREAKFAST", "早餐"),
    LUNCH("LUNCH", "午餐"),
    DINNER("DINNER", "晚餐"),
    SNACK("SNACK", "加餐");

    private final String value;
    private final String desc;

    MealType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
