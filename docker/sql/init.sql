-- ============================================
-- 展览馆学生展示系统 - 数据库初始化脚本
-- Database: MySQL 8.0+
-- Charset: utf8mb4
-- ============================================

DROP DATABASE IF EXISTS exhibition_db;
CREATE DATABASE exhibition_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE exhibition_db;

-- ============================================
-- 1. 二级学院表
-- ============================================
DROP TABLE IF EXISTS college;
CREATE TABLE college (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学院ID',
  name        VARCHAR(100) NOT NULL COMMENT '学院名称',
  code        VARCHAR(20)  NOT NULL UNIQUE COMMENT '学院编码',
  sort_order  INT          DEFAULT 0 COMMENT '排序',
  status      TINYINT      DEFAULT 1 COMMENT '1启用 0禁用',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='二级学院';

-- ============================================
-- 2. 用户表（学生/校友/各级管理员）
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username         VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名/学号',
  password         VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
  real_name        VARCHAR(50)  NOT NULL COMMENT '真实姓名',
  role             TINYINT      NOT NULL COMMENT '1学生 2校友 3院级审核 4教务处 5校级管理员',
  college_id       BIGINT       COMMENT '所属学院',
  major            VARCHAR(50)  COMMENT '专业',
  enrollment_year  INT          COMMENT '入学年份',
  graduation_year  INT          COMMENT '毕业年份',
  phone            VARCHAR(20)  COMMENT '联系电话',
  email            VARCHAR(100) COMMENT '邮箱',
  avatar_url       VARCHAR(255) COMMENT '头像URL',
  bio              VARCHAR(500) COMMENT '个人简介',
  status           TINYINT      DEFAULT 1 COMMENT '1正常 0禁用',
  last_login_at    DATETIME     COMMENT '最后登录时间',
  created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_role (role),
  INDEX idx_college (college_id),
  INDEX idx_grad_year (graduation_year)
) ENGINE=InnoDB COMMENT='用户表';

-- ============================================
-- 3. 校友资料主表（核心）
-- ============================================
DROP TABLE IF EXISTS alumni_profile;
CREATE TABLE alumni_profile (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资料ID',
  user_id         BIGINT       NOT NULL COMMENT '所属用户',
  title           VARCHAR(200) NOT NULL COMMENT '资料标题',
  category        TINYINT      NOT NULL COMMENT '1荣誉 2作品 3成绩 4活动 5其他',
  description     TEXT         COMMENT '描述',
  cover_url       VARCHAR(255) COMMENT '封面图URL',
  achieve_date    DATE         COMMENT '取得日期',
  achieve_level   VARCHAR(50)  COMMENT '获奖级别(国家级/省级/校级)',
  issuing_org     VARCHAR(200) COMMENT '颁发机构',
  status          TINYINT      DEFAULT 0 COMMENT '0草稿 1院审中 2教务审中 3已发布 4驳回',
  reject_reason   VARCHAR(500) COMMENT '驳回原因',
  is_on_shelf     TINYINT      DEFAULT 0 COMMENT '0未上架 1已上架',
  display_weight  INT          DEFAULT 0 COMMENT '展示权重(越大越靠前)',
  view_count      INT          DEFAULT 0 COMMENT '浏览次数',
  current_auditor BIGINT       COMMENT '当前审核人',
  created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_status (status),
  INDEX idx_shelf (is_on_shelf, display_weight),
  INDEX idx_category (category)
) ENGINE=InnoDB COMMENT='校友资料主表';

-- ============================================
-- 4. 资料附件表（图片/视频/文档）
-- ============================================
DROP TABLE IF EXISTS alumni_media;
CREATE TABLE alumni_media (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  profile_id    BIGINT       NOT NULL COMMENT '资料ID',
  media_type    TINYINT      NOT NULL COMMENT '1图片 2视频 3文档',
  file_url      VARCHAR(255) NOT NULL COMMENT '文件URL',
  thumbnail_url VARCHAR(255) COMMENT '缩略图URL',
  file_name     VARCHAR(200) COMMENT '原始文件名',
  file_size     BIGINT       COMMENT '文件大小(字节)',
  duration      INT          COMMENT '视频时长(秒)',
  width         INT          COMMENT '宽度',
  height        INT          COMMENT '高度',
  sort_order    INT          DEFAULT 0,
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_profile (profile_id)
) ENGINE=InnoDB COMMENT='资料附件';

-- ============================================
-- 5. 审核记录表
-- ============================================
DROP TABLE IF EXISTS audit_log;
CREATE TABLE audit_log (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  profile_id  BIGINT      NOT NULL COMMENT '资料ID',
  auditor_id  BIGINT      NOT NULL COMMENT '审核人ID',
  audit_level TINYINT     NOT NULL COMMENT '1院级 2校级',
  result      TINYINT     NOT NULL COMMENT '1通过 2驳回',
  comment     VARCHAR(500) COMMENT '审核意见',
  created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_profile (profile_id),
  INDEX idx_auditor (auditor_id)
) ENGINE=InnoDB COMMENT='审核记录';

-- ============================================
-- 6. 人脸特征表
-- ============================================
DROP TABLE IF EXISTS face_feature;
CREATE TABLE face_feature (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id         BIGINT      NOT NULL UNIQUE COMMENT '用户ID',
  feature_data    BLOB        NOT NULL COMMENT '人脸特征向量(1032字节)',
  feature_version VARCHAR(20) DEFAULT 'arcface_v4' COMMENT '算法版本',
  face_image_url  VARCHAR(255) COMMENT '采集时的人脸图URL(可选)',
  created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='人脸特征';

-- ============================================
-- 7. 展示标签表
-- ============================================
DROP TABLE IF EXISTS display_tag;
CREATE TABLE display_tag (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  name       VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名',
  color      VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
  sort_order INT         DEFAULT 0,
  created_at DATETIME    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='展示标签';

DROP TABLE IF EXISTS profile_tag;
CREATE TABLE profile_tag (
  profile_id BIGINT NOT NULL,
  tag_id     BIGINT NOT NULL,
  PRIMARY KEY (profile_id, tag_id),
  INDEX idx_tag (tag_id)
) ENGINE=InnoDB COMMENT='资料-标签关联';

-- ============================================
-- 8. 大屏识别日志（用于行为分析）
-- ============================================
DROP TABLE IF EXISTS recognize_log;
CREATE TABLE recognize_log (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       COMMENT '识别到的用户ID(NULL=未识别)',
  device_id   VARCHAR(50)  COMMENT '设备标识',
  match_score FLOAT        COMMENT '匹配相似度',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_time (created_at)
) ENGINE=InnoDB COMMENT='识别日志';

-- ============================================
-- 9. 系统配置表
-- ============================================
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
  config_key   VARCHAR(50) PRIMARY KEY,
  config_value VARCHAR(500),
  description  VARCHAR(200),
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统配置';

-- ============================================
-- 初始化数据
-- ============================================

-- 学院
-- ============================================
-- 初始化数据（续）
-- ============================================

-- 学院
INSERT INTO college(name, code, sort_order) VALUES
('计算机与信息工程学院', 'CS', 1),
('机械工程学院', 'ME', 2),
('经济管理学院', 'EM', 3),
('外国语学院', 'FL', 4),
('艺术设计学院', 'AD', 5);

-- 默认账号（密码统一为 123456，BCrypt加密）
INSERT INTO sys_user(username, password, real_name, role, college_id) VALUES
('admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '校级管理员', 5, NULL),
('jiaowu',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '教务处审核员', 4, NULL),
('cs_audit',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '计信院审核员', 3, 1),
('student01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张三', 1, 1),
('alumni01',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李四', 2, 1);

UPDATE sys_user SET major='软件工程', enrollment_year=2020, graduation_year=2024 WHERE username='student01';
UPDATE sys_user SET major='计算机科学与技术', enrollment_year=2016, graduation_year=2020 WHERE username='alumni01';

-- 标签
INSERT INTO display_tag(name, color) VALUES
('国家级',     '#F56C6C'),
('省级',       '#E6A23C'),
('校级',       '#409EFF'),
('科技创新',   '#67C23A'),
('文体艺术',   '#9C27B0'),
('社会实践',   '#00BCD4'),
('学术成果',   '#FF5722');

-- 系统配置
INSERT INTO sys_config(config_key, config_value, description) VALUES
('carousel.interval',        '8',     '默认轮播间隔(秒)'),
('personal.duration',        '30',    '个人专属模式持续(秒)'),
('face.match.threshold',     '0.82',  '人脸匹配阈值'),
('face.detect.interval',     '500',   '人脸检测间隔(毫秒)'),
('display.max.items',        '50',    '大屏最多展示条数');

-- ⚠️ 默认密码 BCrypt 密文已使用 BCryptPasswordEncoder.encode("123456") 生成，可直接使用。