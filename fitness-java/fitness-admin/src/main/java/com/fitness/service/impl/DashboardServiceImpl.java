package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.Course;
import com.fitness.entity.TrainingRecord;
import com.fitness.entity.User;
import com.fitness.mapper.CourseMapper;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalCourses", courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, "APPROVED")));
        data.put("todayActiveUsers", trainingRecordMapper.selectCount(
                new LambdaQueryWrapper<TrainingRecord>().eq(TrainingRecord::getRecordDate, LocalDate.now())));
        return data;
    }
}
