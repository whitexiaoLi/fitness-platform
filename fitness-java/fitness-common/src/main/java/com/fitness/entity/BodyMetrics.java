package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("body_metrics")
public class BodyMetrics extends BaseEntity {
    private Long userId;
    private BigDecimal weight;
    private BigDecimal bodyFat;
    private BigDecimal bmi;
    private BigDecimal waist;
    private BigDecimal hip;
    private LocalDate recordDate;
}
