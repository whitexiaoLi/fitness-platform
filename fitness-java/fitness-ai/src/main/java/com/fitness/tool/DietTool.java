package com.fitness.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.DietRecord;
import com.fitness.mapper.DietRecordMapper;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DietTool {

    @Autowired
    private DietRecordMapper dietRecordMapper;

    @Tool("查询用户某日饮食记录，返回总热量和三大营养素汇总")
    public Map<String, Object> queryDietRecords(Long userId, LocalDate date) {
        List<DietRecord> records = dietRecordMapper.selectList(
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date.toString());
        result.put("totalCalories", totalCalories);
        result.put("totalProtein", totalProtein);
        result.put("totalCarbs", totalCarbs);
        result.put("totalFat", totalFat);
        result.put("records", records);
        return result;
    }
}
