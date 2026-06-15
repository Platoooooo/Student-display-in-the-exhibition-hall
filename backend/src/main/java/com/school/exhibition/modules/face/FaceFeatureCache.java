package com.school.exhibition.modules.face;

import com.school.exhibition.modules.face.entity.FaceFeature;
import com.school.exhibition.modules.face.mapper.FaceFeatureMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 人脸特征内存缓存
 * 启动时加载全部特征到内存，定时刷新，避免每次识别都全表扫描
 * 生产环境特征量 >10 万时应替换为向量数据库（Milvus/PgVector）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FaceFeatureCache {

    private final FaceFeatureMapper faceFeatureMapper;

    /** 使用 CopyOnWriteArrayList 支持高并发读 + 低频率写 */
    private final CopyOnWriteArrayList<FaceFeature> cache = new CopyOnWriteArrayList<>();

    private final AtomicLong lastLoadTime = new AtomicLong(0);

    @PostConstruct
    public void init() {
        refresh();
    }

    /** 每 60 秒从数据库增量刷新缓存 */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void refresh() {
        try {
            List<FaceFeature> features = faceFeatureMapper.selectList(null);
            cache.clear();
            cache.addAll(features);
            lastLoadTime.set(System.currentTimeMillis());
            log.debug("[FaceCache] 刷新完成, size={}", features.size());
        } catch (Exception e) {
            log.error("[FaceCache] 刷新失败", e);
        }
    }

    /** 获取所有特征（高性能读，无锁） */
    public List<FaceFeature> getAll() {
        return cache;
    }

    /** 特征数量 */
    public int size() {
        return cache.size();
    }

    /** 上次加载时间 */
    public long lastLoadTime() {
        return lastLoadTime.get();
    }
}
