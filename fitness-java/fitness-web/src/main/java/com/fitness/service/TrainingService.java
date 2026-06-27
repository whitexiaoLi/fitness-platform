package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.TrainingRecord;
import java.time.LocalDate;
import java.util.Map;

public interface TrainingService {
    TrainingRecord addRecord(Long userId, TrainingRecord record);
    Page<TrainingRecord> listRecords(Long userId, int page, int size, LocalDate start, LocalDate end);
    TrainingRecord getRecord(Long id);
    TrainingRecord updateRecord(Long id, Long userId, TrainingRecord record);
    void deleteRecord(Long id, Long userId);
    Map<String, Object> getStats(Long userId, LocalDate start, LocalDate end);
}
