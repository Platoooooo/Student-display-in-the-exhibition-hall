package com.school.exhibition.modules.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProfileSaveRequest {
    /** null 表示新增，非 null 表示更新 */
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotNull(message = "类目不能为空")
    private Integer category;

    private String description;
    private String coverUrl;
    private LocalDate achieveDate;
    private String achieveLevel;
    private String issuingOrg;

    /** 媒体附件列表 */
    private List<MediaDTO> mediaList;

    /** 标签 ID 列表 */
    private List<Long> tagIds;
}
