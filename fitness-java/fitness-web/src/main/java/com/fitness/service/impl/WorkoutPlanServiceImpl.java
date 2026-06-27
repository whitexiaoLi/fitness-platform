package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.WorkoutPlan;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.WorkoutPlanMapper;
import com.fitness.service.WorkoutPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    @Autowired
    private WorkoutPlanMapper mapper;

    @Override
    public List<WorkoutPlan> listByUser(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<WorkoutPlan>()
                        .eq(WorkoutPlan::getUserId, userId)
                        .orderByDesc(WorkoutPlan::getCreateTime));
    }

    @Override
    public WorkoutPlan create(Long userId, WorkoutPlan plan) {
        plan.setId(null);
        plan.setUserId(userId);
        mapper.insert(plan);
        return plan;
    }

    @Override
    public WorkoutPlan update(Long id, Long userId, WorkoutPlan plan) {
        WorkoutPlan existing = mapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        plan.setId(id);
        plan.setUserId(userId);
        mapper.updateById(plan);
        return mapper.selectById(id);
    }

    @Override
    public void delete(Long id, Long userId) {
        WorkoutPlan existing = mapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        mapper.deleteById(id);
    }
}
