package com.school.exhibition.modules.profile.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alumni_media")
public class AlumniMedia {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long profileId;
    /** 1图片 2视频 3文档 */
    private Integer mediaType;
    private String fileUrl;
    private String thumbnailUrl;
    private String fileName;
    private Long fileSize;
    private Integer duration;
    private Integer width;
    private Integer height;
    private Integer sortOrder;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
