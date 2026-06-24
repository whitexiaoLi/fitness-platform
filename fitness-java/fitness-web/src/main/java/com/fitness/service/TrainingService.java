package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.BodyMetrics;
import com.fitness.entity.DietRecord;
import com.fitness.entity.TrainingRecord;
import java.time.LocalDate;

public interface TrainingService {
    TrainingRecord addRecord(Long userId, TrainingRecord record);
    Page<TrainingRecord> listRecords(Long userId, int page, int size, LocalDate start, LocalDate end);
    TrainingRecord getRecord(Long id);
}
