package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.UserVO;

public interface AdminCoachService {
    Page<UserVO> listCoaches(int page, int size, String keyword);
    UserVO updateCoachProfile(Long id, String bio, String certifications, String specialties,
                              Integer experience, java.math.BigDecimal hourlyRate);
}
