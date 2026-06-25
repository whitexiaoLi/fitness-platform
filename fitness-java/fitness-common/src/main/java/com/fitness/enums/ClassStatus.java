package com.fitness.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum ClassStatus implements IEnum<String> {
    UPCOMING("UPCOMING", "即将开始"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    private final String value;
    private final String label;

    ClassStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
