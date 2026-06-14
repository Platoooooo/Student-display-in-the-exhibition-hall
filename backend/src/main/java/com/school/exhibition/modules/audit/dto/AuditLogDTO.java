package com.school.exhibition.modules.audit.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDTO {
    private Long id;
    private Long profileId;
    private Long auditorId;
    private String auditorName;
    private Integer auditLevel;
    private Integer result;
    private String comment;
    private LocalDateTime createdAt;
}
