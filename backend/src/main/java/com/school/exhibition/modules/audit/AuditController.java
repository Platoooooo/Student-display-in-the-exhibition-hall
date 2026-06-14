package com.school.exhibition.modules.audit;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.school.exhibition.common.result.PageResult;
import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.audit.dto.AuditLogDTO;
import com.school.exhibition.modules.audit.dto.AuditRequest;
import com.school.exhibition.modules.profile.dto.ProfileDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /** 待审列表：院级/教务/超管 */
    @GetMapping("/pending")
    @SaCheckRole(value = {"3", "4", "5"}, mode = SaMode.OR)
    public R<PageResult<ProfileDTO>> pending(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(auditService.pending(page, size));
    }

    /** 审核（通过/驳回） */
    @PostMapping("/{id}/audit")
    @SaCheckRole(value = {"3", "4", "5"}, mode = SaMode.OR)
    public R<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditRequest req) {
        auditService.doAudit(id, req);
        return R.ok();
    }

    /** 审核历史 */
    @GetMapping("/{id}/history")
    public R<List<AuditLogDTO>> history(@PathVariable Long id) {
        return R.ok(auditService.history(id));
    }
}
