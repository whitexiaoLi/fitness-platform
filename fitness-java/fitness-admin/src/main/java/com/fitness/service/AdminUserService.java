package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.UserVO;
import com.fitness.entity.User;

public interface AdminUserService {
    Page<UserVO> listUsers(int page, int size, String keyword, String role, Integer status);
    UserVO getUserDetail(Long id);
    UserVO createUser(String username, String password, String nickname, String role, String phone);
    void updateUserStatus(Long id, Integer status);
    void updateUserRole(Long id, String role);
}
