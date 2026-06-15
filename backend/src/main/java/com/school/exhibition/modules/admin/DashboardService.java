package com.school.exhibition.modules.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.modules.college.entity.College;
import com.school.exhibition.modules.college.mapper.CollegeMapper;
import com.school.exhibition.modules.face.entity.RecognizeLog;
import com.school.exhibition.modules.face.mapper.RecognizeLogMapper;
import com.school.exhibition.modules.profile.ProfileService;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import com.school.exhibition.modules.profile.mapper.AlumniProfileMapper;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AlumniProfileMapper profileMapper;
    private final SysUserMapper userMapper;
    private final CollegeMapper collegeMapper;
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

        // 按学院统计：单条 SQL 聚合 + 一次学院列表查询
        List<College> colleges = collegeMapper.selectList(
                Wrappers.<College>lambdaQuery().orderByAsc(College::getSortOrder));
        Map<Long, Long> collegeCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : profileMapper.countPublishedByCollege()) {
            Object cid = row.get("collegeId");
            Object total = row.get("total");
            if (cid != null && total != null) {
                collegeCountMap.put(((Number) cid).longValue(), ((Number) total).longValue());
            }
        }
        Map<String, Long> byCollege = new LinkedHashMap<>();
        for (College c : colleges) {
            byCollege.put(c.getName(), collegeCountMap.getOrDefault(c.getId(), 0L));
        }
        s.setByCollege(byCollege);

        // 按类目统计：单条 SQL 聚合
        String[] catNames = {"", "荣誉", "作品", "成绩", "活动", "其他"};
        Map<Integer, Long> catCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : profileMapper.countPublishedByCategory()) {
            Object cat = row.get("category");
            Object total = row.get("total");
            if (cat != null && total != null) {
                catCountMap.put(((Number) cat).intValue(), ((Number) total).longValue());
            }
        }
        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            byCategory.put(catNames[i], catCountMap.getOrDefault(i, 0L));
        }
        s.setByCategory(byCategory);

        // 最近 7 天识别趋势（单条 GROUP BY 替代 7 次独立 COUNT）
        List<DailyCount> trend = new ArrayList<>();
        Map<String, Long> dayMap = new LinkedHashMap<>();
        for (Map<String, Object> row : recognizeLogMapper.countDailyLast7Days()) {
            Object date = row.get("date");
            Object total = row.get("total");
            if (date != null && total != null) {
                dayMap.put(date.toString(), ((Number) total).longValue());
            }
        }
        for (int i = 6; i >= 0; i--) {
            String day = LocalDate.now().minusDays(i).toString();
            DailyCount dc = new DailyCount();
            dc.setDate(day);
            dc.setCount(dayMap.getOrDefault(day, 0L));
            trend.add(dc);
        }
        s.setRecognizeTrend(trend);

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
        private Map<String, Long> byCategory;
        private List<DailyCount> recognizeTrend;
    }

    @Data
    public static class DailyCount {
        private String date;
        private Long count;
    }
}
