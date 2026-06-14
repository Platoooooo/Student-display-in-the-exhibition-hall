package com.school.exhibition.modules.face.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaceRecognizeRequest {
    private String deviceId;
    @NotBlank
    private String featureBase64;
}
