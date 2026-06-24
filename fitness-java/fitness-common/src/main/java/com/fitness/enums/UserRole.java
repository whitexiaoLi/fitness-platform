package com.fitness.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum UserRole implements IEnum<String> {
    USER("USER", "普通用户"),
    COACH("COACH", "教练"),
    ADMIN("ADMIN", "管理员");

    private final String value;
    private final String desc;

    UserRole(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
