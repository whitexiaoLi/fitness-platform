package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Course;

public interface AdminCourseService {
    Page<Course> listPendingCourses(int page, int size);
    void approveCourse(Long id);
    void rejectCourse(Long id, String reason);
}
