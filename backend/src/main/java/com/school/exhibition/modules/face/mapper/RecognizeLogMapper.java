package com.school.exhibition.modules.face.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.exhibition.modules.face.entity.RecognizeLog;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface RecognizeLogMapper extends BaseMapper<RecognizeLog> {

    /** 近7天每日识别量 — 单条 GROUP BY 替代 7 次独立 COUNT */
    @Select("SELECT DATE(created_at) AS date, COUNT(*) AS total " +
            "FROM recognize_log " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> countDailyLast7Days();
}
