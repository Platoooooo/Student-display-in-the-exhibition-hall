package com.school.exhibition.modules.user.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
    private String roleName;
    private Long collegeId;
    private String collegeName;
    private String avatarUrl;
}
