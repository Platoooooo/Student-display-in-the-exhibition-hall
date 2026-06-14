package com.school.exhibition.modules.profile;

import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import com.school.exhibition.modules.profile.dto.ProfileSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /** 保存草稿 */
    @PostMapping("/draft")
    public R<Long> saveDraft(@Valid @RequestBody ProfileSaveRequest req) {
        return R.ok(profileService.saveDraft(req));
    }

    /** 提交（进入院级审核） */
    @PostMapping("/submit")
    public R<Long> submit(@Valid @RequestBody ProfileSaveRequest req) {
        return R.ok(profileService.submit(req));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public R<ProfileDTO> detail(@PathVariable Long id) {
        return R.ok(profileService.detail(id));
    }

    /** 删除（仅草稿/驳回） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        profileService.delete(id);
        return R.ok();
    }

    /** 我的资料分页 */
    @GetMapping("/my")
    public R<PageResult<ProfileDTO>> myList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return R.ok(profileService.myList(page, size, status));
    }
}
