package com.school.exhibition.modules.face;

import com.sun.jna.*;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ArcFace SDK v3.0 原生桥接（JNA）
 * DLL 需放在 java.library.path 中（运行目录或 Windows/System32）
 */
@Slf4j
@Service
public class ArcFaceNativeService {

    @Value("${arcface.app-id:}")
    private String appId;

    @Value("${arcface.sdk-key:}")
    private String sdkKey;

    private Pointer engine;
    private volatile boolean initialized;

    public static final int FEATURE_SIZE = 1032;
    private static final int ASF_DETECT_MODE_VIDEO = 0;
    private static final int ASF_OP_0_ONLY = 1;
    private static final int ASVL_PAF_RGB24_B8G8R8 = 0x201;

    public interface ArcFaceLib extends Library {
        int ASFActivation(String appId, String sdkKey);
        int ASFInitEngine(int detectMode, int orientPriority, int scale,
                          int maxFaceNum, int combinedMask, PointerByReference engine);
        int ASFUninitEngine(Pointer engine);
        int ASFDetectFaces(Pointer engine, int width, int height, int format,
                           Pointer imgData, Pointer detectedFaces);
        int ASFFaceFeatureExtract(Pointer engine, int width, int height, int format,
                                  Pointer imgData, Pointer faceInfo, Pointer feature);
        int ASFFaceFeatureCompare(Pointer engine, Pointer feature1, Pointer feature2,
                                  FloatByReference confidence, int compareModel);
    }

    private ArcFaceLib lib;
    private boolean mockMode = true;

    @PostConstruct
    public void init() {
        if (appId == null || appId.isBlank() || sdkKey == null || sdkKey.isBlank()) {
            log.warn("[ArcFace] appId/sdkKey 未配置，使用 Mock 模式");
            initialized = true;
            return;
        }

        try {
            lib = Native.load("libarcsoft_face_engine", ArcFaceLib.class);

            int rc = lib.ASFActivation(appId, sdkKey);
            log.info("[ArcFace] 激活 rc={}", rc);
            if (rc != 0) {
                log.error("[ArcFace] 激活失败 rc={}，回退 Mock 模式", rc);
                initialized = true;
                return;
            }

            PointerByReference pEngine = new PointerByReference();
            rc = lib.ASFInitEngine(ASF_DETECT_MODE_VIDEO, ASF_OP_0_ONLY,
                    16, 5, 0x5, pEngine);
            log.info("[ArcFace] InitEngine rc={}", rc);
            if (rc == 0 && pEngine.getValue() != null) {
                engine = pEngine.getValue();
                mockMode = false;
                initialized = true;
                log.info("[ArcFace] 真实模式就绪");
            } else {
                log.error("[ArcFace] InitEngine 失败，回退 Mock 模式");
                initialized = true;
            }
        } catch (UnsatisfiedLinkError e) {
            log.error("[ArcFace] DLL 未找到: {}，回退 Mock 模式", e.getMessage());
            initialized = true;
        }
    }

    @PreDestroy
    public void destroy() {
        if (engine != null && lib != null) {
            lib.ASFUninitEngine(engine);
            engine = null;
        }
    }

    public boolean isMockMode() { return mockMode; }

    /** 从 BGR24 字节数组提取人脸特征 (1032 bytes) */
    public byte[] extractFeature(byte[] bgr, int width, int height) {
        if (mockMode) return mockFeature(bgr);
        if (engine == null || lib == null || bgr == null) return null;

        Memory imgMem = null;
        try {
            // 将 byte array 包装为 JNA Memory 传入 native
            imgMem = new Memory(bgr.length);
            imgMem.write(0, bgr, 0, bgr.length);

            // 分配检测结果缓冲区 ASF_MultiFaceInfo: ~208 bytes
            Memory faceInfoBuf = new Memory(208);
            faceInfoBuf.setInt(0, 0); // nFace = 0

            int rc = lib.ASFDetectFaces(engine, width, height, ASVL_PAF_RGB24_B8G8R8,
                    imgMem, faceInfoBuf);
            if (rc != 0) return null;

            int nFace = faceInfoBuf.getInt(0);
            if (nFace <= 0) return null;

            // 取第一张脸的 MRECT (offset 8: skip nFace+reserved)
            int left   = faceInfoBuf.getInt(8);
            int top    = faceInfoBuf.getInt(12);
            int right  = faceInfoBuf.getInt(16);
            int bottom = faceInfoBuf.getInt(20);

            // ASF_SingleFaceInfo: 24 bytes
            Memory singlePtr = new Memory(24);
            singlePtr.setInt(0, left);
            singlePtr.setInt(4, top);
            singlePtr.setInt(8, right);
            singlePtr.setInt(12, bottom);
            singlePtr.setInt(16, faceInfoBuf.getInt(8 + 160)); // lfaceOrient[0]

            // ASF_FaceFeature: 16 bytes (feature pointer + size)
            Memory featurePtr = new Memory(16);
            rc = lib.ASFFaceFeatureExtract(engine, width, height, ASVL_PAF_RGB24_B8G8R8,
                    imgMem, singlePtr, featurePtr);
            if (rc != 0) return null;

            Pointer featBuf = featurePtr.getPointer(0);
            if (featBuf == null) return null;

            byte[] result = new byte[FEATURE_SIZE];
            featBuf.read(0, result, 0, FEATURE_SIZE);
            return result;
        } catch (Exception e) {
            log.error("[ArcFace] extractFeature 异常: {}", e.getMessage());
            return null;
        }
    }

    /** Mock 模式：基于输入生成确定性特征 */
    private byte[] mockFeature(byte[] data) {
        byte[] feature = new byte[FEATURE_SIZE];
        int seed = 0;
        for (int i = 0; i < Math.min(data.length, 512); i++) seed += data[i] * (i + 1);
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 0; i < FEATURE_SIZE / 4; i++) {
            float v = rng.nextFloat() * 2 - 1;
            int bits = Float.floatToIntBits(v);
            int off = i * 4;
            feature[off]     = (byte) (bits & 0xFF);
            feature[off + 1] = (byte) ((bits >> 8) & 0xFF);
            feature[off + 2] = (byte) ((bits >> 16) & 0xFF);
            feature[off + 3] = (byte) ((bits >> 24) & 0xFF);
        }
        return feature;
    }
}
