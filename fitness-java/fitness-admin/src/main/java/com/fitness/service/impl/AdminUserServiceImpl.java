package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.User;
import com.fitness.enums.UserRole;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMapper;
import com.fitness.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<UserVO> listUsers(int page, int size, String keyword, String role, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (StringUtils.hasText(role)) {
            List<UserRole> roles = Arrays.stream(role.split(","))
                    .map(UserRole::valueOf).toList();
            wrapper.in(User::getRole, roles);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<UserVO>) userPage.convert(this::toVO);
    }

    @Override
    public UserVO getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return toVO(user);
    }

    @Override
    public UserVO createUser(String username, String password, String nickname, String role, String phone) {
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exist != null) throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(UserRole.valueOf(role));
        user.setPhone(phone);
        user.setStatus(1);
        userMapper.insert(user);
        return toVO(user);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void updateUserRole(Long id, String role) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        user.setRole(UserRole.valueOf(role));
        userMapper.updateById(user);
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
