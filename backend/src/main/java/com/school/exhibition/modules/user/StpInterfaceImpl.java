package com.school.exhibition.modules.user;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色/权限注入器
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        SysUser u = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getId, userId));
        if (u == null) return Collections.emptyList();
        return List.of(String.valueOf(u.getRole()));
    }
}
