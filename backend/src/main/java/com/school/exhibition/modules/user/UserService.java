package com.school.exhibition.modules.user;

import cn.dev33.satoken.stp.StpUtil;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;

    public SysUser getById(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException("用户不存在");
        return u;
    }

    public Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    public SysUser currentUser() {
        return getById(currentUserId());
    }

    public Integer currentRole() {
        return currentUser().getRole();
    }
}
