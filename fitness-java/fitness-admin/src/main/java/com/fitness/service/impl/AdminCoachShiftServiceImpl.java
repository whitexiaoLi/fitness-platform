package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.CoachShift;
import com.fitness.mapper.CoachShiftMapper;
import com.fitness.service.AdminCoachShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminCoachShiftServiceImpl implements AdminCoachShiftService {

    @Autowired
    private CoachShiftMapper coachShiftMapper;

    @Override
    public List<CoachShift> getShifts(Long coachId) {
        return coachShiftMapper.selectList(
                new LambdaQueryWrapper<CoachShift>()
                        .eq(CoachShift::getCoachId, coachId)
                        .orderByAsc(CoachShift::getDayOfWeek));
    }

    @Override
    @Transactional
    public List<CoachShift> saveShifts(Long coachId, List<CoachShift> shifts) {
        // Delete existing shifts for this coach
        coachShiftMapper.delete(new LambdaQueryWrapper<CoachShift>().eq(CoachShift::getCoachId, coachId));
        // Insert new shifts
        for (CoachShift shift : shifts) {
            shift.setCoachId(coachId);
            coachShiftMapper.insert(shift);
        }
        return shifts;
    }
}
