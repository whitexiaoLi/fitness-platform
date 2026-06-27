package com.fitness.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String role;
    private Integer status;
    private LocalDateTime createTime;

    // Coach-specific
    private String bio;
    private String certifications;
    private String specialties;
    private Integer experience;
    private java.math.BigDecimal hourlyRate;
    private java.math.BigDecimal height;
}
