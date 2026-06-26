package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.DietRecord;
import com.fitness.entity.FoodItem;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.DietRecordMapper;
import com.fitness.mapper.FoodItemMapper;
import com.fitness.service.DietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DietServiceImpl implements DietService {

    @Autowired
    private DietRecordMapper mapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Override
    public DietRecord addRecord(Long userId, DietRecord record) {
        record.setUserId(userId);
        record.setId(null);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        autoCalculate(record);
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
    public DietRecord updateRecord(Long id, Long userId, DietRecord record) {
        DietRecord existing = mapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DIET_RECORD_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIET_RECORD_NOT_OWNER);
        }
        record.setId(id);
        record.setUserId(userId);
        autoCalculate(record);
        mapper.updateById(record);
        return mapper.selectById(id);
    }

    @Override
    public void deleteRecord(Long id, Long userId) {
        DietRecord existing = mapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DIET_RECORD_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIET_RECORD_NOT_OWNER);
        }
        mapper.deleteById(id);
    }

    /**
     * Auto-calculate macros from food_item when foodId + weightGrams is provided.
     * Formula: nutrient = food_item.per100g / 100 * weightGrams
     */
    private void autoCalculate(DietRecord record) {
        if (record.getFoodId() != null && record.getWeightGrams() != null && record.getWeightGrams().compareTo(BigDecimal.ZERO) > 0) {
            FoodItem food = foodItemMapper.selectById(record.getFoodId());
            if (food != null) {
                BigDecimal ratio = record.getWeightGrams().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                if (food.getCalories() != null) {
                    record.setCalories(food.getCalories().multiply(ratio).setScale(0, RoundingMode.HALF_UP).intValue());
                }
                if (food.getProtein() != null) {
                    record.setProtein(food.getProtein().multiply(ratio).setScale(1, RoundingMode.HALF_UP));
                }
                if (food.getCarbs() != null) {
                    record.setCarbs(food.getCarbs().multiply(ratio).setScale(1, RoundingMode.HALF_UP));
                }
                if (food.getFat() != null) {
                    record.setFat(food.getFat().multiply(ratio).setScale(1, RoundingMode.HALF_UP));
                }
                // Override foodName with the food item's name for display
                if (record.getFoodName() == null || record.getFoodName().isBlank()) {
                    record.setFoodName(food.getName());
                }
            }
        }
    }
}
