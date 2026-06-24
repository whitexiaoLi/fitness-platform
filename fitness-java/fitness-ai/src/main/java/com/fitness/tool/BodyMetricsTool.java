package com.fitness.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.BodyMetrics;
import com.fitness.mapper.BodyMetricsMapper;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BodyMetricsTool {

    @Autowired
    private BodyMetricsMapper bodyMetricsMapper;

    @Tool("查询用户身体数据趋势，返回体重和体脂率的变化")
    public Map<String, Object> queryBodyMetrics(Long userId, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        List<BodyMetrics> metrics = bodyMetricsMapper.selectList(
                new LambdaQueryWrapper<BodyMetrics>()
                        .eq(BodyMetrics::getUserId, userId)
                        .ge(BodyMetrics::getRecordDate, since)
                        .orderByAsc(BodyMetrics::getRecordDate));

        BigDecimal latestWeight = null;
        BigDecimal firstWeight = null;
        if (!metrics.isEmpty()) {
            latestWeight = metrics.get(metrics.size() - 1).getWeight();
            firstWeight = metrics.get(0).getWeight();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCount", metrics.size());
        result.put("latestWeight", latestWeight);
        result.put("firstWeight", firstWeight);
        if (firstWeight != null && latestWeight != null) {
            result.put("weightChange", latestWeight.subtract(firstWeight));
        }
        result.put("trend", metrics);
        return result;
    }
}
