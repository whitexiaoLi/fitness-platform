package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private UserRole role;
    private Integer status;
}
