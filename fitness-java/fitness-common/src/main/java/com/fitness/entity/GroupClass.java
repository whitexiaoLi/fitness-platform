package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.enums.ClassStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_class")
public class GroupClass extends BaseEntity {
    private Long courseId;
    private Long coachId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private String location;
    private ClassStatus status;
}
