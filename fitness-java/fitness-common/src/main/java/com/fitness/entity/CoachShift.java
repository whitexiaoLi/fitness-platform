package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach_shift")
public class CoachShift extends BaseEntity {
    private Long coachId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
