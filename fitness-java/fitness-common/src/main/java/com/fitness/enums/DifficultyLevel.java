package com.fitness.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum DifficultyLevel implements IEnum<String> {
    BEGINNER("BEGINNER", "初级"),
    INTERMEDIATE("INTERMEDIATE", "中级"),
    ADVANCED("ADVANCED", "高级");

    private final String value;
    private final String desc;

    DifficultyLevel(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
