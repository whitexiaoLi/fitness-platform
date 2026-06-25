package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.User;
import com.fitness.enums.UserRole;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMapper;
import com.fitness.service.AdminCoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminCoachServiceImpl implements AdminCoachService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<UserVO> listCoaches(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, UserRole.COACH);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<UserVO>) userPage.convert(this::toVO);
    }

    @Override
    public UserVO updateCoachProfile(Long id, String bio, String certifications, String specialties,
                                     Integer experience, java.math.BigDecimal hourlyRate) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        user.setBio(bio);
        user.setCertifications(certifications);
        user.setSpecialties(specialties);
        user.setExperience(experience);
        user.setHourlyRate(hourlyRate);
        userMapper.updateById(user);
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole() != null ? user.getRole().getValue() : null);
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setBio(user.getBio());
        vo.setCertifications(user.getCertifications());
        vo.setSpecialties(user.getSpecialties());
        vo.setExperience(user.getExperience());
        vo.setHourlyRate(user.getHourlyRate());
        return vo;
    }
}
