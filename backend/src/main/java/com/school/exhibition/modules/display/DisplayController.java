package com.school.exhibition.modules.display;

import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.face.FaceService;
import com.school.exhibition.modules.face.dto.FaceRecognizeRequest;
import com.school.exhibition.modules.face.dto.FaceRecognizeResult;
import jakarta.validation.Valid;
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

    /** 大屏列表模式：分页 */
    @GetMapping("/list")
    public R<PageResult<ProfileDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) Integer category) {
        return R.ok(displayService.getList(page, size, category));
    }

    /** 大屏搜索 */
    @GetMapping("/search")
    public R<List<ProfileDTO>> search(@RequestParam String keyword) {
        return R.ok(displayService.search(keyword));
    }

    /** 单条资料详情 */
    @GetMapping("/profile/{id}")
    public R<ProfileDTO> profile(@PathVariable Long id) {
        return R.ok(displayService.getProfileDetail(id));
    }

    /** 人脸识别比对（Unity 上报特征） */
    @PostMapping("/face/recognize")
    public R<FaceRecognizeResult> recognize(@Valid @RequestBody FaceRecognizeRequest req) {
        return R.ok(faceService.recognize(req));
    }
}