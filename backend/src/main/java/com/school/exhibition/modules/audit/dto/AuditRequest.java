package com.school.exhibition.modules.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {
    /** 1通过 2驳回 */
    @NotNull
    private Integer result;
    private String comment;
}
