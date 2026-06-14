package com.school.exhibition.modules.face.dto;

import com.school.exhibition.modules.profile.dto.ProfileDTO;
import lombok.Data;

import java.util.List;

@Data
public class FaceRecognizeResult {
    private boolean matched;
    private Long userId;
    private String userName;
    private float score;
    private List<ProfileDTO> profiles;
}
