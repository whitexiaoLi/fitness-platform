package com.fitness.service;

import com.fitness.entity.BodyMetrics;
import java.time.LocalDate;
import java.util.List;

public interface BodyMetricsService {
    BodyMetrics addRecord(Long userId, BodyMetrics record);
    List<BodyMetrics> listRecords(Long userId, LocalDate start, LocalDate end);
    BodyMetrics getLatest(Long userId);
}
