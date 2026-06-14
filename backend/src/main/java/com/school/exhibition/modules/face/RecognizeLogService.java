package com.school.exhibition.modules.face;

import com.school.exhibition.modules.face.entity.RecognizeLog;
import com.school.exhibition.modules.face.mapper.RecognizeLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecognizeLogService {

    private final RecognizeLogMapper mapper;

    @Async
    public void logAsync(Long userId, String deviceId, float score) {
        try {
            RecognizeLog l = new RecognizeLog();
            l.setUserId(userId);
            l.setDeviceId(deviceId);
            l.setMatchScore(score);
            mapper.insert(l);
        } catch (Exception e) {
            log.warn("[RecognizeLog] 写入失败: {}", e.getMessage());
        }
    }
}
