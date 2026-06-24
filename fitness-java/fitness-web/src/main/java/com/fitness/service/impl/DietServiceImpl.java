package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.DietRecord;
import com.fitness.mapper.DietRecordMapper;
import com.fitness.service.DietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DietServiceImpl implements DietService {

    @Autowired
    private DietRecordMapper mapper;

    @Override
    public DietRecord addRecord(Long userId, DietRecord record) {
        record.setUserId(userId);
        record.setId(null);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        mapper.insert(record);
        return record;
    }

    @Override
    public Map<String, Object> listByDate(Long userId, LocalDate date) {
        List<DietRecord> records = mapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, date));

        int totalCalories = records.stream().mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0).sum();
        BigDecimal totalProtein = records.stream()
                .map(r -> r.getProtein() != null ? r.getProtein() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarbs = records.stream()
                .map(r -> r.getCarbs() != null ? r.getCarbs() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFat = records.stream()
                .map(r -> r.getFat() != null ? r.getFat() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("totalCalories", totalCalories);
        result.put("totalProtein", totalProtein);
        result.put("totalCarbs", totalCarbs);
        result.put("totalFat", totalFat);
        return result;
    }

    @Override
    public void deleteRecord(Long id) {
        mapper.deleteById(id);
    }
}
