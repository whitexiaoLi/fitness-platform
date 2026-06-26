package com.fitness.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private BigDecimal rating;
    private Integer ratingCount;
    private List<CourseExerciseVO> exercises;
    private LocalDateTime createTime;

    @Data
    public static class CourseExerciseVO {
        private Long id;
        private String title;
        private String videoUrl;
        private String description;
        private Integer sortOrder;
    }
}
