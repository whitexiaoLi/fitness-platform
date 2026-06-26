package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.UserMealType;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMealTypeMapper;
import com.fitness.service.UserMealTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMealTypeServiceImpl implements UserMealTypeService {

    @Autowired
    private UserMealTypeMapper mapper;

    @Override
    public List<UserMealType> listByUser(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<UserMealType>()
                        .eq(UserMealType::getUserId, userId)
                        .orderByAsc(UserMealType::getSortOrder));
    }

    @Override
    public UserMealType create(Long userId, UserMealType mealType) {
        mealType.setId(null);
        mealType.setUserId(userId);
        if (mealType.getIsActive() == null) mealType.setIsActive(1);
        if (mealType.getSortOrder() == null) {
            Long count = mapper.selectCount(
                    new LambdaQueryWrapper<UserMealType>().eq(UserMealType::getUserId, userId));
            mealType.setSortOrder(count.intValue());
        }
        mapper.insert(mealType);
        return mealType;
    }

    @Override
    public UserMealType update(Long id, Long userId, UserMealType mealType) {
        UserMealType existing = mapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        mealType.setId(id);
        mealType.setUserId(userId);
        mapper.updateById(mealType);
        return mapper.selectById(id);
    }

    @Override
    public void delete(Long id, Long userId) {
        UserMealType existing = mapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        mapper.deleteById(id);
    }
}
