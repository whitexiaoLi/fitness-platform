package com.fitness.service;

import com.fitness.entity.DietRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DietService {
    DietRecord addRecord(Long userId, DietRecord record);
    Map<String, Object> listByDate(Long userId, LocalDate date);
    void deleteRecord(Long id);
}
