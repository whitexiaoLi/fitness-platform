package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.WorkoutPlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkoutPlanMapper extends BaseMapper<WorkoutPlan> {
}
