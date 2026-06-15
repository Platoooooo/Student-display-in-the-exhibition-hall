package com.school.exhibition.modules.face;

import com.school.exhibition.modules.face.dto.*;
import com.school.exhibition.modules.face.entity.FaceFeature;
import com.school.exhibition.modules.profile.ProfileService;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceFeatureCache faceFeatureCache;
    private final ProfileService profileService;
    private final RecognizeLogService recognizeLogService;

    @Value("${face.match.threshold:0.82}")
    private float threshold;

    /**
     * 人脸特征比对
     * 优先从内存缓存读取特征向量，避免每次全表扫描。
     * 生产环境特征量 >10 万时应升级为向量数据库（Milvus/PgVector）。
     */
    public FaceRecognizeResult recognize(FaceRecognizeRequest req) {
        byte[] target = Base64.getDecoder().decode(req.getFeatureBase64());

        List<FaceFeature> all = faceFeatureCache.getAll();
        FaceFeature best = null;
        float bestScore = 0f;

        for (FaceFeature f : all) {
            byte[] featureData = f.getFeatureData();
            if (featureData == null || featureData.length == 0) continue;
            float score = cosineSimilarity(target, featureData);
            if (score > bestScore) {
                bestScore = score;
                best = f;
            }
        }

        FaceRecognizeResult result = new FaceRecognizeResult();
        if (best != null && bestScore >= threshold) {
            result.setMatched(true);
            result.setUserId(best.getUserId());
            result.setScore(bestScore);

            var user = profileService.getUserBrief(best.getUserId());
            result.setUserName(user.getRealName());

            // 加载该用户的已发布资料
            List<ProfileDTO> profiles = profileService.getPublishedByUserId(best.getUserId());
            result.setProfiles(profiles);

            log.info("识别成功 user={} score={} cacheSize={}", best.getUserId(), bestScore, all.size());
        } else {
            result.setMatched(false);
        }

        // 记录日志（异步）
        recognizeLogService.logAsync(
                result.isMatched() ? result.getUserId() : null,
                req.getDeviceId(),
                bestScore);

        return result;
    }

    /** 余弦相似度（适配 1032 字节 ArcFace 特征，按 float[] 解析） */
    private float cosineSimilarity(byte[] a, byte[] b) {
        if (a.length != b.length)
            return 0f;
        // ArcFace 特征虽是 byte[]，但内部按 float 比对（4字节一组）
        int len = a.length / 4;
        float dot = 0f, na = 0f, nb = 0f;
        for (int i = 0; i < len; i++) {
            float fa = bytesToFloat(a, i * 4);
            float fb = bytesToFloat(b, i * 4);
            dot += fa * fb;
            na += fa * fa;
            nb += fb * fb;
        }
        if (na == 0 || nb == 0)
            return 0f;
        return (float) (dot / (Math.sqrt(na) * Math.sqrt(nb)));
    }

    private float bytesToFloat(byte[] arr, int offset) {
        int bits = ((arr[offset] & 0xff))
                | ((arr[offset + 1] & 0xff) << 8)
                | ((arr[offset + 2] & 0xff) << 16)
                | ((arr[offset + 3] & 0xff) << 24);
        return Float.intBitsToFloat(bits);
    }
}