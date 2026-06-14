package com.school.exhibition.modules.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.tag.entity.DisplayTag;
import com.school.exhibition.modules.tag.mapper.DisplayTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SaCheckRole(value = {"4", "5"}, mode = SaMode.OR)
public class AdminController {

    private final AdminProfileService adminProfileService;
    private final DashboardService dashboardService;
    private final DisplayTagMapper tagMapper;

    /** 资料库（仅已发布） */
    @GetMapping("/profile/library")
    public R<PageResult<ProfileDTO>> library(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer isOnShelf,
            @RequestParam(required = false) String keyword) {
        return R.ok(adminProfileService.library(page, size, category, isOnShelf, keyword));
    }

    /** 上架/下架 */
    @PutMapping("/profile/{id}/shelf")
    public R<Void> shelf(@PathVariable Long id, @RequestParam Integer onShelf) {
        adminProfileService.setShelf(id, onShelf);
        return R.ok();
    }

    /** 设置权重 */
    @PutMapping("/profile/{id}/weight")
    public R<Void> weight(@PathVariable Long id, @RequestParam Integer weight) {
        adminProfileService.setWeight(id, weight);
        return R.ok();
    }

    /** 设置标签 */
    @PostMapping("/profile/{id}/tags")
    public R<Void> tags(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        adminProfileService.setTags(id, body.getOrDefault("tagIds", List.of()));
        return R.ok();
    }

    /** 标签列表 */
    @GetMapping("/tags")
    public R<List<DisplayTag>> tags() {
        return R.ok(tagMapper.selectList(null));
    }

    /** Dashboard */
    @GetMapping("/dashboard")
    public R<DashboardService.DashboardStat> dashboard() {
        return R.ok(dashboardService.overview());
    }
}
