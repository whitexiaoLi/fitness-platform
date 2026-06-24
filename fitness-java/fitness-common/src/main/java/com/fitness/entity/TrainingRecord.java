package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_record")
public class TrainingRecord extends BaseEntity {
    private Long userId;
    private Long courseId;
    private Integer duration;
    private Integer calories;
    private String notes;
    private LocalDate recordDate;
}
