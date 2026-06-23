-- 修复用户中文名
UPDATE sys_user SET real_name = '校级管理员' WHERE id = 1;
UPDATE sys_user SET real_name = '教务处审核员' WHERE id = 2;
UPDATE sys_user SET real_name = '计信院审核员' WHERE id = 3;
UPDATE sys_user SET real_name = '张三' WHERE id = 4;
UPDATE sys_user SET real_name = '李四' WHERE id = 5;

-- 修复学院名称
UPDATE college SET name = '计算机与信息工程学院', code = 'CS' WHERE id = 1;
UPDATE college SET name = '机械工程学院', code = 'ME' WHERE id = 2;
UPDATE college SET name = '经济管理学院', code = 'EM' WHERE id = 3;
UPDATE college SET name = '外国语学院', code = 'FL' WHERE id = 4;
UPDATE college SET name = '艺术设计学院', code = 'AD' WHERE id = 5;
