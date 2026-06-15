package com.school.exhibition.modules.user;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.secure.BCrypt;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.college.entity.College;
import com.school.exhibition.modules.college.mapper.CollegeMapper;
import com.school.exhibition.modules.user.dto.ChangePasswordRequest;
import com.school.exhibition.modules.user.dto.UpdateProfileRequest;
import com.school.exhibition.modules.user.dto.UserDTO;
import com.school.exhibition.modules.user.dto.UserSaveRequest;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * 用户管理 - 仅校级管理员可操作
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final UserService userService;

    /** 分页查询 */
    @GetMapping("/list")
    @SaCheckRole(value = {"4", "5"}, mode = SaMode.OR)
    public R<PageResult<UserDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> p = new Page<>(page, size);
        IPage<SysUser> r = userMapper.selectPage(p,
                Wrappers.<SysUser>lambdaQuery()
                        .eq(role != null, SysUser::getRole, role)
                        .eq(collegeId != null, SysUser::getCollegeId, collegeId)
                        .and(keyword != null && !keyword.isBlank(), w -> w
                                .like(SysUser::getUsername, keyword)
                                .or().like(SysUser::getRealName, keyword))
                        .orderByDesc(SysUser::getCreatedAt));
        Map<Long, String> collegeMap = new HashMap<>();
        collegeMapper.selectList(null).forEach(c -> collegeMap.put(c.getId(), c.getName()));

        List<UserDTO> records = r.getRecords().stream().map(u -> {
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(u, dto);
            dto.setRoleName(Roles.name(u.getRole()));
            if (u.getCollegeId() != null) dto.setCollegeName(collegeMap.get(u.getCollegeId()));
            return dto;
        }).toList();
        return R.ok(PageResult.of(r.getTotal(), records));
    }

    /** 创建/更新 */
    @PostMapping("/save")
    @SaCheckRole("5")
    public R<Long> save(@Valid @RequestBody UserSaveRequest req) {
        SysUser u;
        boolean isNew = req.getId() == null;
        if (isNew) {
            // 用户名唯一性
            Long count = userMapper.selectCount(
                    Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, req.getUsername()));
            if (count > 0) throw new BusinessException("用户名已存在");
            if (req.getPassword() == null || req.getPassword().isBlank())
                throw new BusinessException("初始密码不能为空");
            u = new SysUser();
            BeanUtils.copyProperties(req, u, "password");
            u.setPassword(BCrypt.hashpw(req.getPassword()));
            if (u.getStatus() == null) u.setStatus(1);
            userMapper.insert(u);
        } else {
            u = userMapper.selectById(req.getId());
            if (u == null) throw new BusinessException("用户不存在");
            String oldPwd = u.getPassword();
            BeanUtils.copyProperties(req, u, "password", "username");  // 用户名不可改
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                u.setPassword(BCrypt.hashpw(req.getPassword()));
            } else {
                u.setPassword(oldPwd);
            }
            userMapper.updateById(u);
        }
        return R.ok(u.getId());
    }

    /** 启用/禁用 */
    @PutMapping("/{id}/status")
    @SaCheckRole("5")
    public R<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException("用户不存在");
        u.setStatus(status);
        userMapper.updateById(u);
        return R.ok();
    }

    /** 重置密码（管理员重置为指定密码） */
    @PutMapping("/{id}/reset-password")
    @SaCheckRole("5")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException("用户不存在");
        u.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(u);
        return R.ok();
    }

    /** 用户自己修改密码 */
    @PutMapping("/change-password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        SysUser u = userService.currentUser();
        if (!BCrypt.checkpw(req.getOldPassword(), u.getPassword()))
            throw new BusinessException("原密码错误");
        u.setPassword(BCrypt.hashpw(req.getNewPassword()));
        userMapper.updateById(u);
        return R.ok();
    }

    /** 用户自己更新个人资料（头像/电话/邮箱/简介等） */
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody UpdateProfileRequest body) {
        SysUser u = userService.currentUser();
        if (body.getAvatarUrl() != null) u.setAvatarUrl(body.getAvatarUrl());
        if (body.getPhone() != null) u.setPhone(body.getPhone());
        if (body.getEmail() != null) u.setEmail(body.getEmail());
        if (body.getBio() != null) u.setBio(body.getBio());
        userMapper.updateById(u);
        return R.ok();
    }

    /** 详情 */
    @GetMapping("/{id}")
    @SaCheckRole(value = {"4", "5"}, mode = SaMode.OR)
    public R<UserDTO> detail(@PathVariable Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException("用户不存在");
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(u, dto);
        dto.setRoleName(Roles.name(u.getRole()));
        if (u.getCollegeId() != null) {
            College c = collegeMapper.selectById(u.getCollegeId());
            if (c != null) dto.setCollegeName(c.getName());
        }
        return R.ok(dto);
    }
}
