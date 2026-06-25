package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.CoachStudent;

public interface AdminCoachStudentService {
    Page<CoachStudent> listBindings(int page, int size, Long coachId, Long studentId, String status);
    CoachStudent createBinding(Long coachId, Long studentId);
    void endBinding(Long id);
}
