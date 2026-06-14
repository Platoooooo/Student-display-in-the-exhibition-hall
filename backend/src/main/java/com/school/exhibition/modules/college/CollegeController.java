package com.school.exhibition.modules.college;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.college.entity.College;
import com.school.exhibition.modules.college.mapper.CollegeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeMapper collegeMapper;

    @GetMapping("/list")
    public R<List<College>> list() {
        return R.ok(collegeMapper.selectList(
                Wrappers.<College>lambdaQuery()
                        .eq(College::getStatus, 1)
                        .orderByAsc(College::getSortOrder)));
    }
}
