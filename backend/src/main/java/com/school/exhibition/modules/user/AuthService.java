package com.school.exhibition.modules.user;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.modules.college.entity.College;
import com.school.exhibition.modules.college.mapper.CollegeMapper;
import com.school.exhibition.modules.user.dto.LoginRequest;
import com.school.exhibition.modules.user.dto.LoginResponse;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final CollegeMapper collegeMapper;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        SysUser user = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, req.getUsername()));
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getStatus() != null && user.getStatus() == 0) throw new BusinessException("账号已禁用");
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword()))
            throw new BusinessException("密码错误");

        StpUtil.login(user.getId());
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        LoginResponse rsp = new LoginResponse();
        rsp.setToken(StpUtil.getTokenValue());
        rsp.setUserId(user.getId());
        rsp.setUsername(user.getUsername());
        rsp.setRealName(user.getRealName());
        rsp.setRole(user.getRole());
        rsp.setRoleName(Roles.name(user.getRole()));
        rsp.setCollegeId(user.getCollegeId());
        rsp.setAvatarUrl(user.getAvatarUrl());
        if (user.getCollegeId() != null) {
            College c = collegeMapper.selectById(user.getCollegeId());
            if (c != null) rsp.setCollegeName(c.getName());
        }
        return rsp;
    }

    public void logout() {
        StpUtil.logout();
    }

    public LoginResponse currentUser() {
        long uid = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(uid);
        if (user == null) throw new BusinessException("用户不存在");
        LoginResponse rsp = new LoginResponse();
        rsp.setToken(StpUtil.getTokenValue());
        rsp.setUserId(user.getId());
        rsp.setUsername(user.getUsername());
        rsp.setRealName(user.getRealName());
        rsp.setRole(user.getRole());
        rsp.setRoleName(Roles.name(user.getRole()));
        rsp.setCollegeId(user.getCollegeId());
        rsp.setAvatarUrl(user.getAvatarUrl());
        if (user.getCollegeId() != null) {
            College c = collegeMapper.selectById(user.getCollegeId());
            if (c != null) rsp.setCollegeName(c.getName());
        }
        return rsp;
    }
}
