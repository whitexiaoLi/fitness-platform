package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.dto.response.CoachPerformanceVO;
import com.fitness.entity.*;
import com.fitness.enums.UserRole;
import com.fitness.mapper.*;
import com.fitness.service.CoachPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoachPerformanceServiceImpl implements CoachPerformanceService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserCourseMapper userCourseMapper;

    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

    @Override
    public List<CoachPerformanceVO> getPerformance() {
        List<User> coaches = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, UserRole.COACH));
        List<CoachPerformanceVO> result = new ArrayList<>();

        for (User coach : coaches) {
            CoachPerformanceVO vo = new CoachPerformanceVO();
            vo.setCoachId(coach.getId());
            vo.setCoachName(coach.getNickname());

            // Course count
            Long courseCount = courseMapper.selectCount(
                    new LambdaQueryWrapper<Course>().eq(Course::getCoachId, coach.getId()));
            vo.setCourseCount(courseCount.intValue());

            // Student count
            List<Course> courses = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>().eq(Course::getCoachId, coach.getId()));
            List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
            if (!courseIds.isEmpty()) {
                Long sc = userCourseMapper.selectCount(
                        new LambdaQueryWrapper<UserCourse>().in(UserCourse::getCourseId, courseIds));
                vo.setStudentCount(sc.intValue());
            } else {
                vo.setStudentCount(0);
            }

            // Total revenue
            BigDecimal revenue = BigDecimal.ZERO;
            for (Course course : courses) {
                Long enrolled = userCourseMapper.selectCount(
                        new LambdaQueryWrapper<UserCourse>().eq(UserCourse::getCourseId, course.getId()));
                if (course.getPrice() != null && enrolled != null) {
                    revenue = revenue.add(course.getPrice().multiply(BigDecimal.valueOf(enrolled)));
                }
            }
            vo.setTotalRevenue(revenue);

            // Total training duration
            if (!courseIds.isEmpty()) {
                List<TrainingRecord> records = trainingRecordMapper.selectList(
                        new LambdaQueryWrapper<TrainingRecord>().in(TrainingRecord::getCourseId, courseIds));
                long totalMinutes = records.stream()
                        .mapToLong(r -> r.getDuration() != null ? r.getDuration().longValue() : 0L)
                        .sum();
                vo.setTotalDuration((int) totalMinutes);
            } else {
                vo.setTotalDuration(0);
            }

            result.add(vo);
        }
        return result;
    }
}
