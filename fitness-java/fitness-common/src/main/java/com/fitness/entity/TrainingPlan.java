package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_plan")
public class TrainingPlan extends BaseEntity {
    private Long coachId;
    private Long userId;
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
}
