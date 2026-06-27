package com.fitness.service.impl;

import com.fitness.dto.response.UserVO;
import com.fitness.entity.User;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMapper;
import com.fitness.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return toVO(user);
    }

    @Override
    public UserVO updateProfile(Long userId, String nickname, String avatarUrl, String phone, String height,
                                 String bio, String certifications, String specialties, String experience, String hourlyRate) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (nickname != null) user.setNickname(nickname);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        if (phone != null) user.setPhone(phone);
        if (height != null && !height.isBlank()) user.setHeight(new java.math.BigDecimal(height));
        if (bio != null) user.setBio(bio);
        if (certifications != null) user.setCertifications(certifications);
        if (specialties != null) user.setSpecialties(specialties);
        if (experience != null && !experience.isBlank()) user.setExperience(Integer.valueOf(experience));
        if (hourlyRate != null && !hourlyRate.isBlank()) user.setHourlyRate(new java.math.BigDecimal(hourlyRate));
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole().getValue());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setBio(user.getBio());
        vo.setCertifications(user.getCertifications());
        vo.setSpecialties(user.getSpecialties());
        vo.setExperience(user.getExperience());
        vo.setHourlyRate(user.getHourlyRate());
        vo.setHeight(user.getHeight());
        return vo;
    }
}
