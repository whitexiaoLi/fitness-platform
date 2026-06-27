package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.BodyMetrics;
import com.fitness.entity.User;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.BodyMetricsMapper;
import com.fitness.mapper.UserMapper;
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

    @Autowired
    private UserMapper userMapper;

    @Override
    public BodyMetrics addRecord(Long userId, BodyMetrics record) {
        record.setUserId(userId);
        record.setId(null);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        autoCalculateBmi(userId, record);
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

    @Override
    public BodyMetrics updateRecord(Long id, Long userId, BodyMetrics record) {
        BodyMetrics existing = mapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.METRICS_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.METRICS_NOT_OWNER);
        }
        record.setId(id);
        record.setUserId(userId);
        autoCalculateBmi(userId, record);
        mapper.updateById(record);
        return mapper.selectById(id);
    }

    @Override
    public void deleteRecord(Long id, Long userId) {
        BodyMetrics existing = mapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.METRICS_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.METRICS_NOT_OWNER);
        }
        mapper.deleteById(id);
    }

    private void autoCalculateBmi(Long userId, BodyMetrics record) {
        if (record.getWeight() != null && record.getWeight().compareTo(BigDecimal.ZERO) > 0) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getHeight() != null && user.getHeight().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal heightM = user.getHeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                BigDecimal bmi = record.getWeight().divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
                record.setBmi(bmi);
            }
        }
    }
}
