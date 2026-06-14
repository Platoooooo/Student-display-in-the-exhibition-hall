package com.school.exhibition.modules.display;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.modules.profile.ProfileService;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import com.school.exhibition.modules.profile.mapper.AlumniProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisplayService {

    private final AlumniProfileMapper profileMapper;
    private final ProfileService profileService;

    @Value("${display.max-items:50}")
    private int maxItems;

    /** 大屏轮播：已发布 + 已上架，按权重 desc + updatedAt desc 排序 */
    public List<ProfileDTO> getPlaylist() {
        List<AlumniProfile> list = profileMapper.selectList(Wrappers.<AlumniProfile>lambdaQuery()
                .eq(AlumniProfile::getStatus, ProfileService.STATUS_PUBLISHED)
                .eq(AlumniProfile::getIsOnShelf, 1)
                .orderByDesc(AlumniProfile::getDisplayWeight)
                .orderByDesc(AlumniProfile::getUpdatedAt)
                .last("limit " + maxItems));
        return list.stream().map(p -> profileService.toDTO(p, true)).toList();
    }

    public ProfileDTO getProfileDetail(Long id) {
        AlumniProfile p = profileMapper.selectById(id);
        if (p == null) throw new BusinessException("资料不存在");
        // 浏览次数+1
        p.setViewCount((p.getViewCount() == null ? 0 : p.getViewCount()) + 1);
        profileMapper.updateById(p);
        return profileService.toDTO(p, true);
    }
}
