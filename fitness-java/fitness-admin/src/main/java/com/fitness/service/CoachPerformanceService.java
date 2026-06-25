package com.fitness.service;

import com.fitness.dto.response.CoachPerformanceVO;
import java.util.List;

public interface CoachPerformanceService {
    List<CoachPerformanceVO> getPerformance();
}
