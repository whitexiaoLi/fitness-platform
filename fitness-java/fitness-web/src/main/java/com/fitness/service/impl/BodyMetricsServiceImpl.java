package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.BodyMetrics;
import com.fitness.mapper.BodyMetricsMapper;
import com.fitness.service.BodyMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class BodyMetricsServiceImpl implements BodyMetricsService {

    @Autowired
    private BodyMetricsMapper mapper;

    @Override
    public BodyMetrics addRecord(Long userId, BodyMetrics record) {
        record.setUserId(userId);
        record.setId(null);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        // auto-calculate BMI
        if (record.getWeight() != null) {
            // BMI = weight(kg) / height(m)^2 (estimate height as not stored, use weight formula or skip)
            // Here we just store what user provides; BMI can be calculated on frontend
        }
        mapper.insert(record);
        return record;
    }

    @Override
    public List<BodyMetrics> listRecords(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<BodyMetrics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BodyMetrics::getUserId, userId);
        if (start != null) wrapper.ge(BodyMetrics::getRecordDate, start);
        if (end != null) wrapper.le(BodyMetrics::getRecordDate, end);
        wrapper.orderByAsc(BodyMetrics::getRecordDate);
        return mapper.selectList(wrapper);
    }

    @Override
    public BodyMetrics getLatest(Long userId) {
        return mapper.selectOne(new LambdaQueryWrapper<BodyMetrics>()
                .eq(BodyMetrics::getUserId, userId)
                .orderByDesc(BodyMetrics::getRecordDate)
                .last("LIMIT 1"));
    }
}
