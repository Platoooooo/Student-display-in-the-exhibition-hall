package com.school.exhibition.modules.face.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.exhibition.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("face_feature")
public class FaceFeature extends BaseEntity {
    private Long userId;
    private byte[] featureData;
    private String featureVersion;
    private String faceImageUrl;
}
