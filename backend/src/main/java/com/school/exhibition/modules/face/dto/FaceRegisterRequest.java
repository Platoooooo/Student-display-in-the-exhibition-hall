package com.school.exhibition.modules.face.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaceRegisterRequest {
    @NotBlank
    private String featureBase64;
    private String faceImageUrl;
}
