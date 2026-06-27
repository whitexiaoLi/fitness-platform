package com.fitness.service;

import com.fitness.dto.response.UserVO;

public interface UserService {
    UserVO getProfile(Long userId);
    UserVO updateProfile(Long userId, String nickname, String avatarUrl, String phone, String height,
                          String bio, String certifications, String specialties, String experience, String hourlyRate);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
