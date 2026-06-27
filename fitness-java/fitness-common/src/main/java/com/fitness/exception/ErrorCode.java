package com.fitness.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 1xxx
    USERNAME_EXISTS(1001, "用户名已存在"),
    LOGIN_FAILED(1002, "用户名或密码错误"),
    TOKEN_EXPIRED(1003, "Token 已过期"),
    TOKEN_INVALID(1004, "Token 无效"),
    USER_DISABLED(1005, "用户已被禁用"),
    PASSWORD_ERROR(1006, "原密码错误"),

    // 课程相关 2xxx
    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_ALREADY_SUBSCRIBED(2002, "已订阅该课程"),
    COURSE_NOT_APPROVED(2003, "课程未通过审核"),

    // 教练相关 3xxx
    COACH_PERMISSION_DENIED(3001, "非教练用户无权操作"),

    // AI 相关 4xxx
    AI_SERVICE_ERROR(4001, "AI 服务异常"),
    AI_RATE_LIMIT(4002, "AI 请求过于频繁"),

    // 饮食相关 5xxx
    DIET_RECORD_NOT_FOUND(5001, "饮食记录不存在"),
    DIET_RECORD_NOT_OWNER(5002, "无权操作该饮食记录"),

    // 训练相关 6xxx
    TRAINING_RECORD_NOT_FOUND(6001, "训练记录不存在"),
    TRAINING_RECORD_NOT_OWNER(6002, "无权操作该训练记录"),

    // 身体数据相关 7xxx
    METRICS_NOT_FOUND(7001, "身体数据记录不存在"),
    METRICS_NOT_OWNER(7002, "无权操作该身体数据记录");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
