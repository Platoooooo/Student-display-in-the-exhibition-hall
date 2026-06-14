package com.school.exhibition.modules.college.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.exhibition.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("college")
public class College extends BaseEntity {
    private String name;
    private String code;
    private Integer sortOrder;
    private Integer status;
}
