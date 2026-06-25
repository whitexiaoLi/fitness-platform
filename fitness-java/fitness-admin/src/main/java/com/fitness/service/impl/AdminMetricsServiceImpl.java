package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.BodyMetrics;
import com.fitness.mapper.BodyMetricsMapper;
import com.fitness.service.AdminMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminMetricsServiceImpl implements AdminMetricsService {

    @Autowired
    private BodyMetricsMapper bodyMetricsMapper;

    @Override
    public Page<BodyMetrics> listMetrics(int page, int size, Long userId) {
        LambdaQueryWrapper<BodyMetrics> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(BodyMetrics::getUserId, userId);
        }
        wrapper.orderByDesc(BodyMetrics::getRecordDate);
        return bodyMetricsMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
