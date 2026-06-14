package com.school.exhibition.modules.display;

import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.face.FaceService;
import com.school.exhibition.modules.face.dto.FaceRecognizeRequest;
import com.school.exhibition.modules.face.dto.FaceRecognizeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/display")
@RequiredArgsConstructor
public class DisplayController {

    private final DisplayService displayService;
    private final FaceService faceService;

    /** 大屏轮播列表（已上架 + 按权重排序） */
    @GetMapping("/playlist")
    public R<List<ProfileDTO>> playlist() {
        return R.ok(displayService.getPlaylist());
    }

    /** 单条资料详情 */
    @GetMapping("/profile/{id}")
    public R<ProfileDTO> profile(@PathVariable Long id) {
        return R.ok(displayService.getProfileDetail(id));
    }

    /** 人脸识别比对（Unity 上报特征） */
    @PostMapping("/face/recognize")
    public R<FaceRecognizeResult> recognize(@RequestBody FaceRecognizeRequest req) {
        return R.ok(faceService.recognize(req));
    }
}