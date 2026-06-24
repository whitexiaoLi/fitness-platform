package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.CourseVO;
import com.fitness.entity.Course;
import com.fitness.enums.CourseCategory;
import com.fitness.enums.CourseStatus;
import com.fitness.enums.DifficultyLevel;

public interface CourseService {
    Page<CourseVO> listCourses(int page, int size, String keyword, CourseCategory category, DifficultyLevel difficulty);
    CourseVO getCourse(Long id);
    void subscribe(Long userId, Long courseId);
    Page<CourseVO> listMyCourses(Long userId, int page, int size);
}
