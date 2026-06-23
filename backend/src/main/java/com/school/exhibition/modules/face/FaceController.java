package com.school.exhibition.modules.face;

import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.face.dto.FaceExtractRequest;
import com.school.exhibition.modules.face.dto.FaceRecognizeRequest;
import com.school.exhibition.modules.face.dto.FaceRecognizeResult;
import com.school.exhibition.modules.face.dto.FaceRegisterRequest;
import com.school.exhibition.modules.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
public class FaceController {

    private final FaceRegisterService faceRegisterService;
    private final ArcFaceNativeService arcFaceNativeService;
    private final FaceService faceService;
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

    /** 从图片提取特征向量（供前端拍照后调用） */
    @PostMapping("/extract")
    public R<Map<String, Object>> extract(@Valid @RequestBody FaceExtractRequest req) {
        try {
            byte[] imgBytes = Base64.getDecoder().decode(req.getImageBase64());
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
            if (img == null) return R.fail(400, "无法解析图片");

            // 转为 BGR24 字节数组
            int w = img.getWidth(), h = img.getHeight();
            byte[] bgr = new byte[w * h * 3];
            int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);
            for (int i = 0; i < pixels.length; i++) {
                int p = pixels[i];
                bgr[i * 3]     = (byte) (p & 0xFF);        // B
                bgr[i * 3 + 1] = (byte) ((p >> 8) & 0xFF);  // G
                bgr[i * 3 + 2] = (byte) ((p >> 16) & 0xFF); // R
            }

            byte[] feature = arcFaceNativeService.extractFeature(bgr, w, h);
            if (feature == null) return R.fail(400, "未检测到人脸，请确保正面清晰照片");
            String featureBase64 = Base64.getEncoder().encodeToString(feature);
            return R.ok(Map.of("featureBase64", featureBase64,
                    "mock", arcFaceNativeService.isMockMode()));
        } catch (Exception e) {
            return R.fail(500, "特征提取失败: " + e.getMessage());
        }
    }

    /** 大屏端识别（接收图片 → 提取特征 → 比对 → 返回匹配结果） */
    @PostMapping("/recognize-image")
    public R<FaceRecognizeResult> recognizeImage(@Valid @RequestBody FaceExtractRequest req) {
        try {
            byte[] imgBytes = Base64.getDecoder().decode(req.getImageBase64());
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
            if (img == null) return R.fail(400, "无法解析图片");

            int w = img.getWidth(), h = img.getHeight();
            byte[] bgr = new byte[w * h * 3];
            int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);
            for (int i = 0; i < pixels.length; i++) {
                int p = pixels[i];
                bgr[i * 3]     = (byte) (p & 0xFF);
                bgr[i * 3 + 1] = (byte) ((p >> 8) & 0xFF);
                bgr[i * 3 + 2] = (byte) ((p >> 16) & 0xFF);
            }

            byte[] feature = arcFaceNativeService.extractFeature(bgr, w, h);
            if (feature == null) return R.fail(400, "未检测到人脸");

            FaceRecognizeRequest recReq = new FaceRecognizeRequest();
            recReq.setFeatureBase64(Base64.getEncoder().encodeToString(feature));
            recReq.setDeviceId(req.getDeviceId() != null ? req.getDeviceId() : "web-display");

            FaceRecognizeResult result = faceService.recognize(recReq);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail(500, "识别失败: " + e.getMessage());
        }
    }
}
