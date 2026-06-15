package com.school.exhibition.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * CORS 跨域配置
 * 生产环境应通过 CORS_ALLOWED_ORIGINS 环境变量显式指定允许的域名
 * 开发环境默认允许 localhost 系列
 */
@Slf4j
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:8080}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("[CORS] 允许来源: {}", allowedOrigins);
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
