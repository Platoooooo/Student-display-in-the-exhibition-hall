package com.school.exhibition.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 默认放行：登录、大屏端、上传健康检查等
            SaRouter.match("/**")
                    .notMatch(
                            "/api/auth/login",
                            "/api/auth/captcha",
                            "/api/display/**",
                            "/ws/display/**",
                            "/api/file/preview/**",
                            "/actuator/**",
                            "/error"
                    )
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
