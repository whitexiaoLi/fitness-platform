package com.fitness.service;

import com.fitness.entity.CoachShift;
import java.util.List;

public interface AdminCoachShiftService {
    List<CoachShift> getShifts(Long coachId);
    List<CoachShift> saveShifts(Long coachId, List<CoachShift> shifts);
}
