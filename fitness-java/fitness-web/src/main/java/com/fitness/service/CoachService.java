package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.CourseVO;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.*;

public interface CoachService {
    // Course management
    Course createCourse(Long coachId, Course course);
    Course updateCourse(Long coachId, Long courseId, Course course);
    Page<Course> listMyCourses(Long coachId, int page, int size);
    void deleteCourse(Long coachId, Long courseId);

    // Student management
    Page<UserVO> listStudents(Long coachId, int page, int size);
    Page<TrainingRecord> getStudentTraining(Long studentId, int page, int size);
    Page<BodyMetrics> getStudentMetrics(Long studentId, int page, int size);

    // Training plan management
    TrainingPlan createPlan(Long coachId, TrainingPlan plan);
    Page<TrainingPlan> listMyPlans(Long coachId, int page, int size);
    Page<TrainingPlan> listUserPlans(Long userId, int page, int size);
}
