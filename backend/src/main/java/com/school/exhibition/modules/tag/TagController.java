package com.school.exhibition.modules.tag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.tag.entity.DisplayTag;
import com.school.exhibition.modules.tag.entity.ProfileTag;
import com.school.exhibition.modules.tag.mapper.DisplayTagMapper;
import com.school.exhibition.modules.tag.mapper.ProfileTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 标签管理 */
@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final DisplayTagMapper tagMapper;
    private final ProfileTagMapper profileTagMapper;

    @GetMapping("/list")
    public R<List<DisplayTag>> list() {
        return R.ok(tagMapper.selectList(
                Wrappers.<DisplayTag>lambdaQuery().orderByAsc(DisplayTag::getSortOrder)));
    }

    @PostMapping("/save")
    @SaCheckRole(value = {"4", "5"}, mode = SaMode.OR)
    public R<Long> save(@RequestBody DisplayTag tag) {
        if (tag.getName() == null || tag.getName().isBlank()) throw new BusinessException("标签名不能为空");
        if (tag.getId() == null) {
            // 名称唯一
            Long count = tagMapper.selectCount(Wrappers.<DisplayTag>lambdaQuery()
                    .eq(DisplayTag::getName, tag.getName()));
            if (count > 0) throw new BusinessException("标签名已存在");
            if (tag.getColor() == null || tag.getColor().isBlank()) tag.setColor("#409EFF");
            tagMapper.insert(tag);
        } else {
            tagMapper.updateById(tag);
        }
        return R.ok(tag.getId());
    }

    @DeleteMapping("/{id}")
    @SaCheckRole("5")
    public R<Void> delete(@PathVariable Long id) {
        Long ref = profileTagMapper.selectCount(
                Wrappers.<ProfileTag>lambdaQuery().eq(ProfileTag::getTagId, id));
        if (ref > 0) throw new BusinessException("标签已被引用，无法删除");
        tagMapper.deleteById(id);
        return R.ok();
    }
}
