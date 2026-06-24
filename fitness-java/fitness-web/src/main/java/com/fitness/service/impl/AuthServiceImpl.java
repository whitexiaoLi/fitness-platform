package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.dto.request.LoginRequest;
import com.fitness.dto.request.RegisterRequest;
import com.fitness.dto.response.LoginResponse;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.User;
import com.fitness.enums.UserRole;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMapper;
import com.fitness.service.AuthService;
import com.fitness.utils.JwtUtils;
import com.fitness.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String role = user.getRole().getValue();
        String accessToken = jwtUtils.generateAccessToken(user.getId(), role);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        // store refresh token in Redis
        redisUtils.set("refresh:" + user.getId() + ":" + jwtUtils.getJti(refreshToken),
                refreshToken, 7, TimeUnit.DAYS);

        UserVO userVO = toUserVO(user);
        return new LoginResponse(accessToken, refreshToken, userVO);
    }

    @Override
    public void register(RegisterRequest request) {
        // check username uniqueness
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.USER);
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        String userId = jwtUtils.getUserId(refreshToken);
        String key = "refresh:" + userId + ":" + jwtUtils.getJti(refreshToken);

        if (!redisUtils.hasKey(key)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // delete old refresh token
        redisUtils.delete(key);

        User user = userMapper.selectById(Long.valueOf(userId));
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String role = user.getRole().getValue();
        String newAccessToken = jwtUtils.generateAccessToken(user.getId(), role);
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getId());

        redisUtils.set("refresh:" + userId + ":" + jwtUtils.getJti(newRefreshToken),
                newRefreshToken, 7, TimeUnit.DAYS);

        return new LoginResponse(newAccessToken, newRefreshToken, toUserVO(user));
    }

    @Override
    public void logout(String accessToken) {
        // blacklist access token
        String jti = jwtUtils.getJti(accessToken);
        long remaining = jwtUtils.getTokenExpire(accessToken);
        if (remaining > 0) {
            redisUtils.set("blacklist:" + jti, "1", remaining, TimeUnit.MILLISECONDS);
        }
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole().getValue());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
