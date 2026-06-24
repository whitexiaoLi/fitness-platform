package com.fitness.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.BodyMetrics;
import com.fitness.entity.TrainingRecord;
import com.fitness.mapper.BodyMetricsMapper;
import com.fitness.mapper.TrainingRecordMapper;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlanTool {

    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

    @Autowired
    private BodyMetricsMapper bodyMetricsMapper;

    @Tool("根据用户目标和当前数据生成结构化训练计划建议")
    public Map<String, Object> generateTrainingPlan(Long userId, String goal, int weeks) {
        // Gather user context
        List<TrainingRecord> recentTraining = trainingRecordMapper.selectList(
                new LambdaQueryWrapper<TrainingRecord>()
                        .eq(TrainingRecord::getUserId, userId)
                        .ge(TrainingRecord::getRecordDate, LocalDate.now().minusDays(30))
                        .orderByDesc(TrainingRecord::getRecordDate));

        BodyMetrics latestMetrics = bodyMetricsMapper.selectOne(
                new LambdaQueryWrapper<BodyMetrics>()
                        .eq(BodyMetrics::getUserId, userId)
                        .orderByDesc(BodyMetrics::getRecordDate)
                        .last("LIMIT 1"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("goal", goal);
        result.put("weeks", weeks);
        result.put("recentTrainingCount", recentTraining.size());
        result.put("latestWeight", latestMetrics != null ? latestMetrics.getWeight() : null);
        result.put("latestBodyFat", latestMetrics != null ? latestMetrics.getBodyFat() : null);
        result.put("recommendation", "Based on user's current data and goal (" + goal
                + "), a " + weeks + "-week plan with progressive overload is suggested.");
        return result;
    }
}
