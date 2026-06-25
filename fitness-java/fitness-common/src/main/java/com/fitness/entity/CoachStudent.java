package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach_student")
public class CoachStudent extends BaseEntity {
    private Long coachId;
    private Long studentId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
