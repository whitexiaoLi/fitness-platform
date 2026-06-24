package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.TrainingRecord;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class TrainingServiceImpl implements TrainingService {

    @Autowired
    private TrainingRecordMapper mapper;

    @Override
    public TrainingRecord addRecord(Long userId, TrainingRecord record) {
        record.setUserId(userId);
        record.setId(null);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        mapper.insert(record);
        return record;
    }

    @Override
    public Page<TrainingRecord> listRecords(Long userId, int page, int size, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getUserId, userId);
        if (start != null) wrapper.ge(TrainingRecord::getRecordDate, start);
        if (end != null) wrapper.le(TrainingRecord::getRecordDate, end);
        wrapper.orderByDesc(TrainingRecord::getRecordDate);
        return mapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public TrainingRecord getRecord(Long id) {
        return mapper.selectById(id);
    }
}
