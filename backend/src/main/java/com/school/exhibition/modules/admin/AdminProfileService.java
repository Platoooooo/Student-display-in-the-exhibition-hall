package com.school.exhibition.modules.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.modules.display.DisplayWebSocketHandler;
import com.school.exhibition.modules.profile.ProfileService;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import com.school.exhibition.modules.profile.mapper.AlumniProfileMapper;
import com.school.exhibition.modules.tag.entity.ProfileTag;
import com.school.exhibition.modules.tag.mapper.ProfileTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProfileService {

    private final AlumniProfileMapper profileMapper;
    private final ProfileTagMapper profileTagMapper;
    private final ProfileService profileService;
    private final DisplayWebSocketHandler wsHandler;

    public PageResult<ProfileDTO> library(Integer page, Integer size,
                                          Integer category, Integer isOnShelf, String keyword) {
        Page<AlumniProfile> p = new Page<>(page, size);
        IPage<AlumniProfile> r = profileMapper.selectPage(p,
                Wrappers.<AlumniProfile>lambdaQuery()
                        .eq(AlumniProfile::getStatus, ProfileService.STATUS_PUBLISHED)
                        .eq(category != null, AlumniProfile::getCategory, category)
                        .eq(isOnShelf != null, AlumniProfile::getIsOnShelf, isOnShelf)
                        .like(keyword != null && !keyword.isBlank(),
                                AlumniProfile::getTitle, keyword)
                        .orderByDesc(AlumniProfile::getDisplayWeight)
                        .orderByDesc(AlumniProfile::getUpdatedAt));
        List<ProfileDTO> list = r.getRecords().stream()
                .map(x -> profileService.toDTO(x, false)).toList();
        return PageResult.of(r.getTotal(), list);
    }

    @Transactional
    public void setShelf(Long id, Integer isOnShelf) {
        AlumniProfile p = profileMapper.selectById(id);
        if (p == null) throw new BusinessException("资料不存在");
        if (p.getStatus() != ProfileService.STATUS_PUBLISHED)
            throw new BusinessException("仅已发布资料可上下架");
        p.setIsOnShelf(isOnShelf);
        profileMapper.updateById(p);
        wsHandler.broadcast("REFRESH_PLAYLIST", String.valueOf(id));
    }

    @Transactional
    public void setWeight(Long id, Integer weight) {
        AlumniProfile p = profileMapper.selectById(id);
        if (p == null) throw new BusinessException("资料不存在");
        p.setDisplayWeight(weight);
        profileMapper.updateById(p);
        wsHandler.broadcast("REFRESH_PLAYLIST", String.valueOf(id));
    }

    @Transactional
    public void setTags(Long id, List<Long> tagIds) {
        AlumniProfile p = profileMapper.selectById(id);
        if (p == null) throw new BusinessException("资料不存在");
        profileTagMapper.delete(Wrappers.<ProfileTag>lambdaQuery().eq(ProfileTag::getProfileId, id));
        if (tagIds != null) {
            for (Long tid : tagIds) {
                ProfileTag pt = new ProfileTag();
                pt.setProfileId(id);
                pt.setTagId(tid);
                profileTagMapper.insert(pt);
            }
        }
    }
}
