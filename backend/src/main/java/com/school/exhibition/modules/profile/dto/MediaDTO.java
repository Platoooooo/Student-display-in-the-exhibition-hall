package com.school.exhibition.modules.profile.dto;

import lombok.Data;

@Data
public class MediaDTO {
    private Long id;
    private Integer mediaType;
    private String fileUrl;
    private String thumbnailUrl;
    private String fileName;
    private Integer duration;
    private Integer width;
    private Integer height;
    private Integer sortOrder;
}
