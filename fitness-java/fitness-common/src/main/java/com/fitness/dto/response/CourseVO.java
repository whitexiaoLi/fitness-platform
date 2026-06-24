package com.fitness.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVO {
    private Long id;
    private Long coachId;
    private String coachName;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private String category;
    private String difficulty;
    private Integer duration;
    private BigDecimal price;
    private String status;
    private Integer progress;
    private LocalDateTime createTime;
}
