package com.school.exhibition.modules.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.modules.college.entity.College;
import com.school.exhibition.modules.college.mapper.CollegeMapper;
import com.school.exhibition.modules.face.entity.RecognizeLog;
import com.school.exhibition.modules.face.mapper.RecognizeLogMapper;
import com.school.exhibition.modules.profile.ProfileService;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import com.school.exhibition.modules.profile.mapper.AlumniProfileMapper;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

        // 按学院统计已发布资料数
        List<College> colleges = collegeMapper.selectList(
                Wrappers.<College>lambdaQuery().orderByAsc(College::getSortOrder));
        Map<Long, Long> userCollegeMap = new HashMap<>();
        userMapper.selectList(null).forEach(u -> userCollegeMap.put(u.getId(), u.getCollegeId()));
        List<AlumniProfile> publishedList = profileMapper.selectList(
                Wrappers.<AlumniProfile>lambdaQuery()
                        .eq(AlumniProfile::getStatus, ProfileService.STATUS_PUBLISHED));

        Map<String, Long> byCollege = new LinkedHashMap<>();
        for (College c : colleges) {
            long count = publishedList.stream().filter(p -> {
                Long cid = userCollegeMap.get(p.getUserId());
                return cid != null && cid.equals(c.getId());
            }).count();
            byCollege.put(c.getName(), count);
        }
        s.setByCollege(byCollege);

        // 按类目统计
        String[] catNames = {"", "荣誉", "作品", "成绩", "活动", "其他"};
        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int code = i;
            byCategory.put(catNames[i], publishedList.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory() == code).count());
        }
        s.setByCategory(byCategory);

        // 最近 7 天识别趋势
        List<DailyCount> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            Long count = recognizeLogMapper.selectCount(Wrappers.<RecognizeLog>lambdaQuery()
                    .ge(RecognizeLog::getCreatedAt, start)
                    .lt(RecognizeLog::getCreatedAt, end));
            DailyCount dc = new DailyCount();
            dc.setDate(day.toString());
            dc.setCount(count);
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
