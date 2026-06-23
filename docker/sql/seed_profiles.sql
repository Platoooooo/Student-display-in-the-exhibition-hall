DELETE FROM alumni_profile WHERE id IN (1,2,3);

INSERT INTO alumni_profile (user_id, title, category, description, achieve_date, achieve_level, issuing_org, status, is_on_shelf, display_weight, view_count, created_at, updated_at) VALUES
(4, 'ICPC 区域赛金奖', 1, '2023年ICPC国际大学生程序设计竞赛亚洲区域赛金奖，团队排名前1%。', '2023-12-01', '国际级', 'ACM-ICPC组委会', 3, 1, 100, 0, NOW(), NOW()),
(5, '全国大学生广告艺术大赛一等奖', 2, '大广赛平面设计类全国一等奖作品《数字时代的乡愁》，被收录于中国美术馆数字典藏。', '2023-09-01', '国家级', '教育部高等教育司', 3, 1, 95, 0, NOW(), NOW()),
(4, '全国大学生田径锦标赛100m冠军', 3, '2024年全国大学生田径锦标赛100米决赛10.28秒夺冠，打破校史纪录。', '2024-06-01', '国家级', '中国大学生体育协会', 3, 1, 90, 0, NOW(), NOW());
