package com.school.exhibition.modules.tag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("profile_tag")
public class ProfileTag {
    private Long profileId;
    private Long tagId;
}
