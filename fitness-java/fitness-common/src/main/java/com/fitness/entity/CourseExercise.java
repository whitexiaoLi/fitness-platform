package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_exercise")
public class CourseExercise extends BaseEntity {
    private Long courseId;
    private String title;
    private String videoUrl;
    private String description;
    private Integer sortOrder;
}
