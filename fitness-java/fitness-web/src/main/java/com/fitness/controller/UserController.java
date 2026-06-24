package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.UserVO;
import com.fitness.security.SecurityUser;
import com.fitness.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ApiResponse<UserVO> getProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@AuthenticationPrincipal SecurityUser securityUser,
                                              @RequestBody Map<String, String> body) {
        Long userId = securityUser.getUser().getId();
        return ApiResponse.success(userService.updateProfile(userId,
                body.get("nickname"), body.get("avatarUrl"), body.get("phone")));
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@AuthenticationPrincipal SecurityUser securityUser,
                                             @RequestBody Map<String, String> body) {
        Long userId = securityUser.getUser().getId();
        userService.updatePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return ApiResponse.success();
    }
}
