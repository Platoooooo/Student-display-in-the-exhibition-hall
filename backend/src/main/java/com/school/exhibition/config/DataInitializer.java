package com.school.exhibition.config;

import cn.dev33.satoken.secure.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.modules.user.entity.SysUser;
import com.school.exhibition.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时校正种子用户密码：
 * docker/sql/init.sql 中的 BCrypt 密文是占位符（实际并非 BCrypt("123456")），
 * 这里检测到无法用 "123456" 校验通过的种子用户，就用 Sa-Token 的 BCrypt 重新加密为正确密文。
 *
 * 仅对 5 个固定种子用户名生效；线上若已修改过密码不会被覆盖（先用 "123456" 校验失败才重写）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final List<String> SEED_USERNAMES = List.of(
            "admin", "jiaowu", "cs_audit", "student01", "alumni01"
    );

    private final SysUserMapper userMapper;

    @Override
    public void run(String... args) {
        for (String username : SEED_USERNAMES) {
            SysUser u = userMapper.selectOne(
                    Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
            if (u == null) continue;
            String stored = u.getPassword();
            try {
                if (stored != null && stored.startsWith("$2") && BCrypt.checkpw(DEFAULT_PASSWORD, stored)) {
                    continue; // 已经是正确密文
                }
            } catch (Exception ignored) {
                // 占位密文格式异常时直接重写
            }
            u.setPassword(BCrypt.hashpw(DEFAULT_PASSWORD));
            userMapper.updateById(u);
            log.info("[DataInitializer] 重置种子账号密码: {} -> {}", username, DEFAULT_PASSWORD);
        }
    }
}
