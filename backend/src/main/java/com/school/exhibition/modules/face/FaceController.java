package com.school.exhibition.modules.face;

import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.face.dto.FaceRegisterRequest;
import com.school.exhibition.modules.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
public class FaceController {

    private final FaceRegisterService faceRegisterService;
    private final UserService userService;

    /** 学生端人脸录入 / 重新录入 */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody FaceRegisterRequest req) {
        faceRegisterService.register(req);
        return R.ok();
    }

    /** 查询当前用户是否已录入 */
    @GetMapping("/status")
    public R<Map<String, Boolean>> status() {
        return R.ok(Map.of("registered", faceRegisterService.hasRegistered(userService.currentUserId())));
    }
}
