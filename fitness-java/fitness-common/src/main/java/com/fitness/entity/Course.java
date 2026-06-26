package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.enums.CourseCategory;
import com.fitness.enums.CourseStatus;
import com.fitness.enums.DifficultyLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {
    private Long coachId;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private CourseCategory category;
    private DifficultyLevel difficulty;
    private Integer duration;
    private BigDecimal price;
    private CourseStatus status;
    private BigDecimal rating;
    private Integer ratingCount;
}
