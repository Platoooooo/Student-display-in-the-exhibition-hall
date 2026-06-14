package com.school.exhibition.modules.user;

public final class Roles {
    private Roles() {}

    public static final int STUDENT = 1;
    public static final int ALUMNI = 2;
    public static final int COLLEGE_AUDITOR = 3;
    public static final int ACADEMIC_AUDITOR = 4;
    public static final int SUPER_ADMIN = 5;

    public static String name(int role) {
        return switch (role) {
            case 1 -> "学生";
            case 2 -> "校友";
            case 3 -> "院级审核";
            case 4 -> "教务处";
            case 5 -> "校级管理员";
            default -> "未知";
        };
    }
}
