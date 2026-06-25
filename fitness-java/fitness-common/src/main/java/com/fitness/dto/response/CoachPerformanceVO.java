package com.fitness.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CoachPerformanceVO {
    private Long coachId;
    private String coachName;
    private Integer studentCount;
    private Integer courseCount;
    private Integer totalDuration;
    private BigDecimal totalRevenue;
}
