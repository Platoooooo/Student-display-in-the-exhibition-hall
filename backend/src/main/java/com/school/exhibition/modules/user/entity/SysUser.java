package com.school.exhibition.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.exhibition.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    /** 1学生 2校友 3院级审核 4教务处 5校级管理员 */
    private Integer role;
    private Long collegeId;
    private String major;
    private Integer enrollmentYear;
    private Integer graduationYear;
    private String phone;
    private String email;
    private String avatarUrl;
    private String bio;
    private Integer status;
    private LocalDateTime lastLoginAt;
}
