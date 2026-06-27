package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.TrainingRecord;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public TrainingRecord updateRecord(Long id, Long userId, TrainingRecord record) {
        TrainingRecord existing = mapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.TRAINING_RECORD_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.TRAINING_RECORD_NOT_OWNER);
        }
        record.setId(id);
        record.setUserId(userId);
        mapper.updateById(record);
        return mapper.selectById(id);
    }

    @Override
    public void deleteRecord(Long id, Long userId) {
        TrainingRecord existing = mapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.TRAINING_RECORD_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.TRAINING_RECORD_NOT_OWNER);
        }
        mapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getStats(Long userId, LocalDate start, LocalDate end) {
        List<TrainingRecord> records = mapper.selectList(
                new LambdaQueryWrapper<TrainingRecord>()
                        .eq(TrainingRecord::getUserId, userId)
                        .ge(start != null, TrainingRecord::getRecordDate, start)
                        .le(end != null, TrainingRecord::getRecordDate, end)
                        .orderByAsc(TrainingRecord::getRecordDate));

        // Unique training days in range
        Set<LocalDate> uniqueDays = records.stream().map(TrainingRecord::getRecordDate).collect(Collectors.toSet());
        int totalDays = uniqueDays.size();

        // Current streak: consecutive days ending at today or end date
        LocalDate checkEnd = end != null ? end : LocalDate.now();
        int currentStreak = 0;
        LocalDate d = checkEnd;
        while (uniqueDays.contains(d)) {
            currentStreak++;
            d = d.minusDays(1);
        }

        // Type distribution
        Map<String, Long> typeCount = records.stream()
                .filter(r -> r.getTrainingType() != null)
                .collect(Collectors.groupingBy(TrainingRecord::getTrainingType, Collectors.counting()));

        // Daily map: date -> count
        Map<LocalDate, Integer> dailyMap = new LinkedHashMap<>();
        for (TrainingRecord r : records) {
            dailyMap.merge(r.getRecordDate(), 1, Integer::sum);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalDays", totalDays);
        result.put("currentStreak", currentStreak);
        result.put("typeDistribution", typeCount);
        result.put("dailyRecords", dailyMap);
        return result;
    }
}
