package com.fitness.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum CourseCategory implements IEnum<String> {
    STRENGTH("STRENGTH", "增肌"),
    FAT_LOSS("FAT_LOSS", "减脂"),
    YOGA("YOGA", "瑜伽"),
    CARDIO("CARDIO", "有氧"),
    STRETCHING("STRETCHING", "拉伸");

    private final String value;
    private final String desc;

    CourseCategory(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
