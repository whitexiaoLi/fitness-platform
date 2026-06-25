package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.OperationLog;
import com.fitness.mapper.OperationLogMapper;
import com.fitness.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public Page<OperationLog> listLogs(int page, int size, String keyword) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(OperationLog::getUsername, keyword)
                    .or().like(OperationLog::getAction, keyword)
                    .or().like(OperationLog::getTarget, keyword));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return operationLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void saveLog(OperationLog log) {
        operationLogMapper.insert(log);
    }
}
