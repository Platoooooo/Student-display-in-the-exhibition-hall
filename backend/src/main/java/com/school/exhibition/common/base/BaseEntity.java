package com.school.exhibition.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {
    @TableId(type = IdType.AUTO)
    protected Long id;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    protected LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updatedAt;
}
