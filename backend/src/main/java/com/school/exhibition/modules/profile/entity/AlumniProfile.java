package com.school.exhibition.modules.profile.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.exhibition.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alumni_profile")
public class AlumniProfile extends BaseEntity {
    private Long userId;
    private String title;
    /** 1荣誉 2作品 3成绩 4活动 5其他 */
    private Integer category;
    private String description;
    private String coverUrl;
    private LocalDate achieveDate;
    private String achieveLevel;
    private String issuingOrg;
    /** 0草稿 1院审中 2教务审中 3已发布 4驳回 */
    private Integer status;
    private String rejectReason;
    private Integer isOnShelf;
    private Integer displayWeight;
    private Integer viewCount;
    private Long currentAuditor;
}
