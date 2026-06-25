package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.OperationLog;

public interface OperationLogService {
    Page<OperationLog> listLogs(int page, int size, String keyword);
    void saveLog(OperationLog log);
}
