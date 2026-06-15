package com.school.exhibition.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 创建/更新用户用 */
@Data
public class UserSaveRequest {
    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;
    /** 创建时必填；编辑可空（保留原密码） */
    private String password;
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    @NotNull(message = "角色不能为空")
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
}
