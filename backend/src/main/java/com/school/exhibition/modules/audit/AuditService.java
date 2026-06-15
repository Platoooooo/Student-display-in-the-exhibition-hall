package com.school.exhibition.modules.audit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.modules.audit.dto.AuditLogDTO;
import com.school.exhibition.modules.audit.dto.AuditRequest;
import com.school.exhibition.modules.audit.entity.AuditLog;
import com.school.exhibition.modules.audit.mapper.AuditLogMapper;
import com.school.exhibition.modules.profile.ProfileService;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import com.school.exhibition.modules.profile.mapper.AlumniProfileMapper;
import com.school.exhibition.modules.user.Roles;
import com.school.exhibition.modules.user.UserService;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AlumniProfileMapper profileMapper;
    private final AuditLogMapper auditLogMapper;
    private final SysUserMapper userMapper;
    private final UserService userService;
    private final ProfileService profileService;

    /**
     * 审核员的待审列表：
     * - 院级审核员（role=3）：看本学院 status=1 的资料
     * - 教务处（role=4）：看 status=2 的资料
     */
    public PageResult<ProfileDTO> pending(Integer page, Integer size) {
        SysUser me = userService.currentUser();
        Page<AlumniProfile> p = new Page<>(page, size);

        var qw = Wrappers.<AlumniProfile>lambdaQuery();
        if (me.getRole() == Roles.COLLEGE_AUDITOR) {
            // 院级：本学院 + status=1
            if (me.getCollegeId() == null) throw new BusinessException("院级审核员未关联学院");
            // 子查询：用户 in (本学院的用户)
            List<Long> userIds = userMapper.selectList(
                    Wrappers.<SysUser>lambdaQuery().eq(SysUser::getCollegeId, me.getCollegeId())
            ).stream().map(SysUser::getId).toList();
            if (userIds.isEmpty()) return PageResult.of(0, List.of());
            qw.in(AlumniProfile::getUserId, userIds)
                    .eq(AlumniProfile::getStatus, ProfileService.STATUS_COLLEGE_AUDIT);
        } else if (me.getRole() == Roles.ACADEMIC_AUDITOR) {
            qw.eq(AlumniProfile::getStatus, ProfileService.STATUS_ACADEMIC_AUDIT);
        } else if (me.getRole() == Roles.SUPER_ADMIN) {
            qw.in(AlumniProfile::getStatus,
                    ProfileService.STATUS_COLLEGE_AUDIT, ProfileService.STATUS_ACADEMIC_AUDIT);
        } else {
            throw new BusinessException("无审核权限");
        }
        qw.orderByAsc(AlumniProfile::getUpdatedAt);

        IPage<AlumniProfile> r = profileMapper.selectPage(p, qw);
        List<ProfileDTO> list = r.getRecords().stream().map(x -> profileService.toDTO(x, true)).toList();
        return PageResult.of(r.getTotal(), list);
    }

    @Transactional
    public void doAudit(Long profileId, AuditRequest req) {
        SysUser me = userService.currentUser();
        AlumniProfile p = profileMapper.selectById(profileId);
        if (p == null) throw new BusinessException("资料不存在");

        int level;
        if (me.getRole() == Roles.COLLEGE_AUDITOR) {
            if (p.getStatus() != ProfileService.STATUS_COLLEGE_AUDIT)
                throw new BusinessException("非院级审核中");
            level = 1;
        } else if (me.getRole() == Roles.ACADEMIC_AUDITOR) {
            if (p.getStatus() != ProfileService.STATUS_ACADEMIC_AUDIT)
                throw new BusinessException("非教务审核中");
            level = 2;
        } else if (me.getRole() == Roles.SUPER_ADMIN) {
            // 校管可同时审 院级/教务，按当前资料状态决定等级
            if (p.getStatus() == ProfileService.STATUS_COLLEGE_AUDIT) {
                level = 1;
            } else if (p.getStatus() == ProfileService.STATUS_ACADEMIC_AUDIT) {
                level = 2;
            } else {
                throw new BusinessException("非审核中状态");
            }
        } else {
            throw new BusinessException("无审核权限");
        }

        // 写审核记录
        AuditLog log = new AuditLog();
        log.setProfileId(profileId);
        log.setAuditorId(me.getId());
        log.setAuditLevel(level);
        log.setResult(req.getResult());
        log.setComment(req.getComment());
        auditLogMapper.insert(log);

        // 更新资料状态
        if (req.getResult() == 1) {
            // 通过
            if (level == 1) {
                p.setStatus(ProfileService.STATUS_ACADEMIC_AUDIT);
                p.setRejectReason(null);
            } else {
                p.setStatus(ProfileService.STATUS_PUBLISHED);
                p.setRejectReason(null);
            }
        } else {
            // 驳回
            p.setStatus(ProfileService.STATUS_REJECTED);
            p.setRejectReason(req.getComment());
        }
        p.setCurrentAuditor(me.getId());
        profileMapper.updateById(p);
    }

    public List<AuditLogDTO> history(Long profileId) {
        List<AuditLog> list = auditLogMapper.selectList(Wrappers.<AuditLog>lambdaQuery()
                .eq(AuditLog::getProfileId, profileId).orderByDesc(AuditLog::getCreatedAt));
        List<AuditLogDTO> result = new ArrayList<>();
        for (AuditLog l : list) {
            AuditLogDTO d = new AuditLogDTO();
            d.setId(l.getId());
            d.setProfileId(l.getProfileId());
            d.setAuditorId(l.getAuditorId());
            d.setAuditLevel(l.getAuditLevel());
            d.setResult(l.getResult());
            d.setComment(l.getComment());
            d.setCreatedAt(l.getCreatedAt());
            SysUser u = userMapper.selectById(l.getAuditorId());
            if (u != null) d.setAuditorName(u.getRealName());
            result.add(d);
        }
        return result;
    }
}
