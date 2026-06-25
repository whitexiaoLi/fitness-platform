package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Course;

public interface AdminCourseService {
    Page<Course> listCourses(int page, int size, String status);
    void approveCourse(Long id);
    void rejectCourse(Long id, String reason);
}
