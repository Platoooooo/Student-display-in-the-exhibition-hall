package com.school.exhibition.modules.display;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.common.result.PageResult;
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

    /** 大屏列表模式：分页（已上架） */
    public PageResult<ProfileDTO> getList(Integer page, Integer size, Integer category) {
        Page<AlumniProfile> p = new Page<>(page, size);
        IPage<AlumniProfile> r = profileMapper.selectPage(p, Wrappers.<AlumniProfile>lambdaQuery()
                .eq(AlumniProfile::getStatus, ProfileService.STATUS_PUBLISHED)
                .eq(AlumniProfile::getIsOnShelf, 1)
                .eq(category != null, AlumniProfile::getCategory, category)
                .orderByDesc(AlumniProfile::getDisplayWeight)
                .orderByDesc(AlumniProfile::getUpdatedAt));
        List<ProfileDTO> list = r.getRecords().stream().map(x -> profileService.toDTO(x, true)).toList();
        return PageResult.of(r.getTotal(), list);
    }

    /** 大屏搜索：按标题模糊匹配 */
    public List<ProfileDTO> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        List<AlumniProfile> list = profileMapper.selectList(Wrappers.<AlumniProfile>lambdaQuery()
                .eq(AlumniProfile::getStatus, ProfileService.STATUS_PUBLISHED)
                .eq(AlumniProfile::getIsOnShelf, 1)
                .like(AlumniProfile::getTitle, keyword)
                .orderByDesc(AlumniProfile::getDisplayWeight)
                .last("limit 30"));
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
