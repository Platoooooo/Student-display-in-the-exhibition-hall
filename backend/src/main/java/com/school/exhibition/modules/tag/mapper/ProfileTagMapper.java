package com.school.exhibition.modules.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.exhibition.modules.tag.entity.ProfileTag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProfileTagMapper extends BaseMapper<ProfileTag> {

    @Select("SELECT t.name FROM display_tag t JOIN profile_tag pt ON pt.tag_id = t.id WHERE pt.profile_id = #{profileId}")
    List<String> selectTagNamesByProfileId(@Param("profileId") Long profileId);
}
