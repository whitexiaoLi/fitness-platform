package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.GroupClass;
import com.fitness.enums.ClassStatus;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.GroupClassMapper;
import com.fitness.service.AdminGroupClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminGroupClassServiceImpl implements AdminGroupClassService {

    @Autowired
    private GroupClassMapper groupClassMapper;

    @Override
    public Page<GroupClass> listClasses(int page, int size, String status) {
        LambdaQueryWrapper<GroupClass> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(GroupClass::getStatus, ClassStatus.valueOf(status));
        } else {
            wrapper.ne(GroupClass::getStatus, ClassStatus.CANCELLED);
        }
        wrapper.orderByAsc(GroupClass::getStartTime);
        return groupClassMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public GroupClass createClass(GroupClass groupClass) {
        groupClass.setStatus(ClassStatus.UPCOMING);
        groupClass.setCurrentEnrollment(0);
        groupClassMapper.insert(groupClass);
        return groupClass;
    }

    @Override
    public GroupClass updateClass(GroupClass groupClass) {
        GroupClass exist = groupClassMapper.selectById(groupClass.getId());
        if (exist == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        groupClassMapper.updateById(groupClass);
        return groupClass;
    }

    @Override
    public void cancelClass(Long id) {
        GroupClass gc = groupClassMapper.selectById(id);
        if (gc == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        gc.setStatus(ClassStatus.CANCELLED);
        groupClassMapper.updateById(gc);
    }
}
