package com.school.exhibition.modules.profile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.modules.college.entity.College;
import com.school.exhibition.modules.college.mapper.CollegeMapper;
import com.school.exhibition.modules.profile.dto.MediaDTO;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.profile.dto.ProfileSaveRequest;
import com.school.exhibition.modules.profile.dto.ProfileUserBrief;
import com.school.exhibition.modules.profile.entity.AlumniMedia;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import com.school.exhibition.modules.profile.mapper.AlumniMediaMapper;
import com.school.exhibition.modules.profile.mapper.AlumniProfileMapper;
import com.school.exhibition.modules.tag.entity.ProfileTag;
import com.school.exhibition.modules.tag.mapper.ProfileTagMapper;
import com.school.exhibition.modules.user.UserService;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfileService {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_COLLEGE_AUDIT = 1;
    public static final int STATUS_ACADEMIC_AUDIT = 2;
    public static final int STATUS_PUBLISHED = 3;
    public static final int STATUS_REJECTED = 4;

    private final AlumniProfileMapper profileMapper;
    private final AlumniMediaMapper mediaMapper;
    private final ProfileTagMapper profileTagMapper;
    private final SysUserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final UserService userService;

    @Transactional
    public Long saveDraft(ProfileSaveRequest req) {
        return saveOrUpdate(req, STATUS_DRAFT);
    }

    @Transactional
    public Long submit(ProfileSaveRequest req) {
        Long id = saveOrUpdate(req, STATUS_COLLEGE_AUDIT);
        return id;
    }

    private Long saveOrUpdate(ProfileSaveRequest req, int status) {
        Long uid = userService.currentUserId();
        AlumniProfile entity;
        boolean isNew = req.getId() == null;
        if (isNew) {
            entity = new AlumniProfile();
            entity.setUserId(uid);
        } else {
            entity = profileMapper.selectById(req.getId());
            if (entity == null) throw new BusinessException("资料不存在");
            if (!Objects.equals(entity.getUserId(), uid))
                throw new BusinessException("无权操作他人资料");
            // 已发布或审核中不允许直接修改：先驳回或撤回
            if (entity.getStatus() == STATUS_COLLEGE_AUDIT
                    || entity.getStatus() == STATUS_ACADEMIC_AUDIT
                    || entity.getStatus() == STATUS_PUBLISHED) {
                throw new BusinessException("当前状态无法修改");
            }
        }
        entity.setTitle(req.getTitle());
        entity.setCategory(req.getCategory());
        entity.setDescription(req.getDescription());
        entity.setCoverUrl(req.getCoverUrl());
        entity.setAchieveDate(req.getAchieveDate());
        entity.setAchieveLevel(req.getAchieveLevel());
        entity.setIssuingOrg(req.getIssuingOrg());
        entity.setStatus(status);
        if (status == STATUS_COLLEGE_AUDIT) entity.setRejectReason(null);

        if (isNew) profileMapper.insert(entity);
        else profileMapper.updateById(entity);

        Long pid = entity.getId();

        // 同步媒体
        mediaMapper.delete(Wrappers.<AlumniMedia>lambdaQuery().eq(AlumniMedia::getProfileId, pid));
        if (req.getMediaList() != null) {
            int order = 0;
            for (MediaDTO m : req.getMediaList()) {
                AlumniMedia am = new AlumniMedia();
                BeanUtils.copyProperties(m, am);
                am.setProfileId(pid);
                am.setSortOrder(order++);
                mediaMapper.insert(am);
            }
        }

        // 同步标签
        profileTagMapper.delete(Wrappers.<ProfileTag>lambdaQuery().eq(ProfileTag::getProfileId, pid));
        if (req.getTagIds() != null) {
            for (Long tagId : req.getTagIds()) {
                ProfileTag pt = new ProfileTag();
                pt.setProfileId(pid);
                pt.setTagId(tagId);
                profileTagMapper.insert(pt);
            }
        }
        return pid;
    }

    public ProfileDTO detail(Long id) {
        AlumniProfile p = profileMapper.selectById(id);
        if (p == null) throw new BusinessException("资料不存在");
        return toDTO(p, true);
    }

    @Transactional
    public void delete(Long id) {
        AlumniProfile p = profileMapper.selectById(id);
        if (p == null) return;
        if (!Objects.equals(p.getUserId(), userService.currentUserId()))
            throw new BusinessException("无权删除");
        if (p.getStatus() == STATUS_PUBLISHED)
            throw new BusinessException("已发布资料请联系管理员下架");
        mediaMapper.delete(Wrappers.<AlumniMedia>lambdaQuery().eq(AlumniMedia::getProfileId, id));
        profileTagMapper.delete(Wrappers.<ProfileTag>lambdaQuery().eq(ProfileTag::getProfileId, id));
        profileMapper.deleteById(id);
    }

    public PageResult<ProfileDTO> myList(Integer page, Integer size, Integer status) {
        Long uid = userService.currentUserId();
        Page<AlumniProfile> p = new Page<>(page, size);
        IPage<AlumniProfile> r = profileMapper.selectPage(p, Wrappers.<AlumniProfile>lambdaQuery()
                .eq(AlumniProfile::getUserId, uid)
                .eq(status != null, AlumniProfile::getStatus, status)
                .orderByDesc(AlumniProfile::getUpdatedAt));
        List<ProfileDTO> list = r.getRecords().stream().map(x -> toDTO(x, false)).toList();
        return PageResult.of(r.getTotal(), list);
    }

    public ProfileUserBrief getUserBrief(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) return null;
        ProfileUserBrief b = new ProfileUserBrief();
        b.setUserId(u.getId());
        b.setRealName(u.getRealName());
        b.setAvatarUrl(u.getAvatarUrl());
        b.setMajor(u.getMajor());
        b.setGraduationYear(u.getGraduationYear());
        if (u.getCollegeId() != null) {
            College c = collegeMapper.selectById(u.getCollegeId());
            if (c != null) b.setCollegeName(c.getName());
        }
        return b;
    }

    public List<ProfileDTO> getPublishedByUserId(Long userId) {
        List<AlumniProfile> list = profileMapper.selectList(Wrappers.<AlumniProfile>lambdaQuery()
                .eq(AlumniProfile::getUserId, userId)
                .eq(AlumniProfile::getStatus, STATUS_PUBLISHED)
                .eq(AlumniProfile::getIsOnShelf, 1)
                .orderByDesc(AlumniProfile::getDisplayWeight)
                .orderByDesc(AlumniProfile::getUpdatedAt));
        return list.stream().map(p -> toDTO(p, true)).toList();
    }

    /** 实体 → DTO，detail=true 时填充媒体/标签/用户冗余字段 */
    public ProfileDTO toDTO(AlumniProfile p, boolean detail) {
        ProfileDTO dto = new ProfileDTO();
        BeanUtils.copyProperties(p, dto);
        ProfileUserBrief brief = getUserBrief(p.getUserId());
        if (brief != null) {
            dto.setUserName(brief.getRealName());
            dto.setAvatarUrl(brief.getAvatarUrl());
            dto.setCollegeName(brief.getCollegeName());
            dto.setMajor(brief.getMajor());
            dto.setGraduationYear(brief.getGraduationYear());
        }
        if (detail) {
            List<AlumniMedia> ms = mediaMapper.selectList(Wrappers.<AlumniMedia>lambdaQuery()
                    .eq(AlumniMedia::getProfileId, p.getId()).orderByAsc(AlumniMedia::getSortOrder));
            List<MediaDTO> mediaList = new ArrayList<>();
            for (AlumniMedia m : ms) {
                MediaDTO mDto = new MediaDTO();
                BeanUtils.copyProperties(m, mDto);
                mediaList.add(mDto);
            }
            dto.setMediaList(mediaList);
            dto.setTags(profileTagMapper.selectTagNamesByProfileId(p.getId()));
        } else {
            dto.setMediaList(Collections.emptyList());
            dto.setTags(Collections.emptyList());
        }
        return dto;
    }
}
