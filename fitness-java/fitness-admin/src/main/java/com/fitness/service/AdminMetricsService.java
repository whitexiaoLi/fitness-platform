package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.BodyMetrics;

public interface AdminMetricsService {
    Page<BodyMetrics> listMetrics(int page, int size, Long userId);
}
