package com.school.exhibition.modules.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String avatarUrl;
    private String phone;
    private String email;
    private String bio;
}
