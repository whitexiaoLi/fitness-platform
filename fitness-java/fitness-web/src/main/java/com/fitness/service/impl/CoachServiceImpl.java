package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.CourseVO;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.*;
import com.fitness.enums.CourseStatus;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.*;
import com.fitness.service.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoachServiceImpl implements CoachService {

    @Autowired private CourseMapper courseMapper;
    @Autowired private UserCourseMapper userCourseMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private TrainingRecordMapper trainingRecordMapper;
    @Autowired private BodyMetricsMapper bodyMetricsMapper;
    @Autowired private TrainingPlanMapper trainingPlanMapper;

    @Override
    @Transactional
    public Course createCourse(Long coachId, Course course) {
        course.setCoachId(coachId);
        course.setStatus(CourseStatus.DRAFT);
        course.setId(null);
        courseMapper.insert(course);
        return course;
    }

    @Override
    @Transactional
    public Course updateCourse(Long coachId, Long courseId, Course course) {
        Course existing = getOwnCourse(coachId, courseId);
        course.setId(courseId);
        course.setCoachId(coachId);
        course.setCreateTime(null);
        course.setUpdateTime(null);
        courseMapper.updateById(course);
        return courseMapper.selectById(courseId);
    }

    @Override
    public Page<Course> listMyCourses(Long coachId, int page, int size) {
        return courseMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Course>().eq(Course::getCoachId, coachId)
                        .orderByDesc(Course::getCreateTime));
    }

    @Override
    @Transactional
    public void deleteCourse(Long coachId, Long courseId) {
        getOwnCourse(coachId, courseId);
        courseMapper.deleteById(courseId);
    }

    @Override
    public Page<UserVO> listStudents(Long coachId, int page, int size) {
        // Find all user_ids who subscribed to this coach's courses
        List<Long> courseIds = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getCoachId, coachId))
                .stream().map(Course::getId).collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return new Page<>(page, size);
        }

        Page<UserCourse> ucPage = userCourseMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UserCourse>().in(UserCourse::getCourseId, courseIds));

        return (Page<UserVO>) ucPage.convert(uc -> {
            User user = userMapper.selectById(uc.getUserId());
            if (user == null) return null;
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setPhone(user.getPhone());
            vo.setCreateTime(user.getCreateTime());
            return vo;
        });
    }

    @Override
    public Page<TrainingRecord> getStudentTraining(Long studentId, int page, int size) {
        return trainingRecordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<TrainingRecord>().eq(TrainingRecord::getUserId, studentId)
                        .orderByDesc(TrainingRecord::getRecordDate));
    }

    @Override
    public Page<BodyMetrics> getStudentMetrics(Long studentId, int page, int size) {
        return bodyMetricsMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<BodyMetrics>().eq(BodyMetrics::getUserId, studentId)
                        .orderByDesc(BodyMetrics::getRecordDate));
    }

    @Override
    @Transactional
    public TrainingPlan createPlan(Long coachId, TrainingPlan plan) {
        plan.setCoachId(coachId);
        plan.setId(null);
        plan.setStatus(0); // in progress
        trainingPlanMapper.insert(plan);
        return plan;
    }

    @Override
    public Page<TrainingPlan> listMyPlans(Long coachId, int page, int size) {
        return trainingPlanMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<TrainingPlan>().eq(TrainingPlan::getCoachId, coachId)
                        .orderByDesc(TrainingPlan::getCreateTime));
    }

    @Override
    public Page<TrainingPlan> listUserPlans(Long userId, int page, int size) {
        return trainingPlanMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<TrainingPlan>().eq(TrainingPlan::getUserId, userId)
                        .orderByDesc(TrainingPlan::getCreateTime));
    }

    private Course getOwnCourse(Long coachId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || !course.getCoachId().equals(coachId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return course;
    }
}
