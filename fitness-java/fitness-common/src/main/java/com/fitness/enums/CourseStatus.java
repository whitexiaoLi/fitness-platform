package com.fitness.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum CourseStatus implements IEnum<String> {
    DRAFT("DRAFT", "草稿"),
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回");

    private final String value;
    private final String desc;

    CourseStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
