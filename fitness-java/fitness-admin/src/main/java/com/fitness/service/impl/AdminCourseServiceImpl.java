package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Course;
import com.fitness.enums.CourseStatus;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.CourseMapper;
import com.fitness.service.AdminCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminCourseServiceImpl implements AdminCourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public Page<Course> listCourses(int page, int size, String status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Course::getStatus, CourseStatus.valueOf(status));
        }
        wrapper.orderByAsc(Course::getCreateTime);
        return courseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void approveCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        course.setStatus(CourseStatus.APPROVED);
        courseMapper.updateById(course);
    }

    @Override
    public void rejectCourse(Long id, String reason) {
        Course course = courseMapper.selectById(id);
        if (course == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        course.setStatus(CourseStatus.REJECTED);
        courseMapper.updateById(course);
    }
}
