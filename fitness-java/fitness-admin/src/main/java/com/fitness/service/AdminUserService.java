package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.User;

public interface AdminUserService {
    Page<UserVO> listUsers(int page, int size, String role, Integer status);
    UserVO getUserDetail(Long id);
    void updateUserStatus(Long id, Integer status);
}
