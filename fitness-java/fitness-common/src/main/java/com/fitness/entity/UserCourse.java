package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_course")
public class UserCourse extends BaseEntity {
    private Long userId;
    private Long courseId;
    private Integer progress;
    private LocalDateTime purchaseTime;
}
