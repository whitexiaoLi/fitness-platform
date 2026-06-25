package com.fitness.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.UserCourse;
import com.fitness.mapper.UserCourseMapper;
import com.fitness.service.AdminBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminBookingServiceImpl implements AdminBookingService {

    @Autowired
    private UserCourseMapper userCourseMapper;

    @Override
    public Page<UserCourse> listBookings(int page, int size) {
        return userCourseMapper.selectPage(
                new Page<>(page, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCourse>()
                        .orderByDesc(UserCourse::getCreateTime));
    }
}
