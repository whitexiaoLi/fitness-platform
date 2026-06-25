package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.CoachStudent;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.CoachStudentMapper;
import com.fitness.service.AdminCoachStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class AdminCoachStudentServiceImpl implements AdminCoachStudentService {

    @Autowired
    private CoachStudentMapper coachStudentMapper;

    @Override
    public Page<CoachStudent> listBindings(int page, int size, Long coachId, Long studentId, String status) {
        LambdaQueryWrapper<CoachStudent> wrapper = new LambdaQueryWrapper<>();
        if (coachId != null) wrapper.eq(CoachStudent::getCoachId, coachId);
        if (studentId != null) wrapper.eq(CoachStudent::getStudentId, studentId);
        if (status != null && !status.isEmpty()) wrapper.eq(CoachStudent::getStatus, status);
        wrapper.orderByDesc(CoachStudent::getCreateTime);
        return coachStudentMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public CoachStudent createBinding(Long coachId, Long studentId) {
        CoachStudent cs = new CoachStudent();
        cs.setCoachId(coachId);
        cs.setStudentId(studentId);
        cs.setStatus("ACTIVE");
        cs.setStartDate(LocalDate.now());
        coachStudentMapper.insert(cs);
        return cs;
    }

    @Override
    public void endBinding(Long id) {
        CoachStudent cs = coachStudentMapper.selectById(id);
        if (cs == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        cs.setStatus("ENDED");
        cs.setEndDate(LocalDate.now());
        coachStudentMapper.updateById(cs);
    }
}
