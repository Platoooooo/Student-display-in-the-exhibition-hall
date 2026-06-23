package com.school.exhibition.modules.face.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaceExtractRequest {
    /** 图片 Base64 编码（不含 data:image 前缀） */
    @NotBlank(message = "图片不能为空")
    private String imageBase64;

    /** 可选：设备标识（大屏识别时） */
    private String deviceId;
}
