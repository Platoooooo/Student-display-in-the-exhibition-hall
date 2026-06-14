package com.school.exhibition.modules.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.modules.face.entity.RecognizeLog;
import com.school.exhibition.modules.face.mapper.RecognizeLogMapper;
import com.school.exhibition.modules.profile.ProfileService;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import com.school.exhibition.modules.profile.mapper.AlumniProfileMapper;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AlumniProfileMapper profileMapper;
    private final SysUserMapper userMapper;
    private final RecognizeLogMapper recognizeLogMapper;

    public DashboardStat overview() {
        DashboardStat s = new DashboardStat();
        s.setUserCount(userMapper.selectCount(null));
        s.setProfileTotal(profileMapper.selectCount(null));
        s.setProfilePublished(profileMapper.selectCount(
                Wrappers.<AlumniProfile>lambdaQuery()
                        .eq(AlumniProfile::getStatus, ProfileService.STATUS_PUBLISHED)));
        s.setProfileOnShelf(profileMapper.selectCount(
                Wrappers.<AlumniProfile>lambdaQuery()
                        .eq(AlumniProfile::getStatus, ProfileService.STATUS_PUBLISHED)
                        .eq(AlumniProfile::getIsOnShelf, 1)));
        s.setProfilePending(profileMapper.selectCount(
                Wrappers.<AlumniProfile>lambdaQuery().in(AlumniProfile::getStatus,
                        ProfileService.STATUS_COLLEGE_AUDIT, ProfileService.STATUS_ACADEMIC_AUDIT)));
        s.setRecognizeToday(recognizeLogMapper.selectCount(
                Wrappers.<RecognizeLog>lambdaQuery().ge(RecognizeLog::getCreatedAt,
                        LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))));

        // 按学院统计
        Map<String, Long> byCollege = new HashMap<>();
        userMapper.selectList(null).forEach(u -> {
            // 简化：实际可改为 SQL group by
        });
        s.setByCollege(byCollege);
        return s;
    }

    @Data
    public static class DashboardStat {
        private Long userCount;
        private Long profileTotal;
        private Long profilePublished;
        private Long profileOnShelf;
        private Long profilePending;
        private Long recognizeToday;
        private Map<String, Long> byCollege;
    }
}
