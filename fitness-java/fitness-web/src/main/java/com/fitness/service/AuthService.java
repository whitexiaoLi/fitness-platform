package com.fitness.service;

import com.fitness.dto.request.LoginRequest;
import com.fitness.dto.request.RegisterRequest;
import com.fitness.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    LoginResponse refresh(String refreshToken);
    void logout(String accessToken);
}
