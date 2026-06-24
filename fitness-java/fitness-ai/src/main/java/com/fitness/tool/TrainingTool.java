package com.fitness.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.TrainingRecord;
import com.fitness.mapper.TrainingRecordMapper;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TrainingTool {

    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

    @Tool("查询用户近期训练记录，返回训练次数、总时长和总消耗热量")
    public Map<String, Object> queryTrainingRecords(Long userId, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        List<TrainingRecord> records = trainingRecordMapper.selectList(
                new LambdaQueryWrapper<TrainingRecord>()
                        .eq(TrainingRecord::getUserId, userId)
                        .ge(TrainingRecord::getRecordDate, since)
                        .orderByDesc(TrainingRecord::getRecordDate));

        int totalMinutes = records.stream().mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        int totalCalories = records.stream().mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0).sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCount", records.size());
        result.put("totalMinutes", totalMinutes);
        result.put("totalCalories", totalCalories);
        result.put("records", records);
        return result;
    }
}
