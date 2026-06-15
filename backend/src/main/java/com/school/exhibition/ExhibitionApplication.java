package com.school.exhibition;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校友成果展览系统 - 后端入口
 */
@SpringBootApplication
@MapperScan("com.school.exhibition.modules.**.mapper")
public class ExhibitionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExhibitionApplication.class, args);
    }
}
