package com.school.exhibition.modules.profile.dto;

import lombok.Data;

@Data
public class ProfileUserBrief {
    private Long userId;
    private String realName;
    private String avatarUrl;
    private String collegeName;
    private String major;
    private Integer graduationYear;
}
