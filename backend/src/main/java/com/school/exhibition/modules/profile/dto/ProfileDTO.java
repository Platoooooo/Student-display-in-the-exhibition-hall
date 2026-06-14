package com.school.exhibition.modules.profile.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProfileDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String avatarUrl;
    private Long collegeId;
    private String collegeName;
    private String major;
    private Integer graduationYear;

    private String title;
    private Integer category;
    private String description;
    private String coverUrl;
    private LocalDate achieveDate;
    private String achieveLevel;
    private String issuingOrg;

    private Integer status;
    private String rejectReason;
    private Integer isOnShelf;
    private Integer displayWeight;
    private Integer viewCount;

    private List<MediaDTO> mediaList;
    private List<String> tags;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
