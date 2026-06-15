package com.school.exhibition.modules.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.exhibition.modules.profile.entity.AlumniProfile;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface AlumniProfileMapper extends BaseMapper<AlumniProfile> {

    /**
     * 已发布资料按学院聚合统计 — 单条 SQL 完成 join，避免在内存里 build userId→collegeId map。
     * 返回 [{collegeId, total}]
     */
    @Select("SELECT u.college_id AS collegeId, COUNT(p.id) AS total " +
            "FROM alumni_profile p INNER JOIN sys_user u ON p.user_id = u.id " +
            "WHERE p.status = 3 GROUP BY u.college_id")
    List<Map<String, Object>> countPublishedByCollege();

    /**
     * 已发布资料按类目聚合统计 — 单条 SQL 完成。
     * 返回 [{category, total}]
     */
    @Select("SELECT category, COUNT(id) AS total FROM alumni_profile " +
            "WHERE status = 3 GROUP BY category")
    List<Map<String, Object>> countPublishedByCategory();
}
