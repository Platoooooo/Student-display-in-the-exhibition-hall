package com.school.exhibition.modules.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 后台用户列表展示用 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private Integer role;
    private String roleName;
    private Long collegeId;
    private String collegeName;
    private String major;
    private Integer enrollmentYear;
    private Integer graduationYear;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
