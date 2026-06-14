# 方案二实施文档（Claude Code 开发版）

> 目标：交付一份精简、可执行、AI 友好的开发蓝图

---

## 1. 技术栈

```yaml
后端:
  框架: Spring Boot 3.2 + Spring Cloud (单体起步，预留拆分)
  ORM: MyBatis Plus
  数据库: MySQL 8.0 + Redis 7
  文件存储: MinIO
  消息推送: WebSocket (Spring 原生)
  工作流: Flowable 7
  认证: Sa-Token

管理后台 + 学生端:
  框架: Vue 3 + TypeScript + Vite
  UI: Element Plus
  状态: Pinia
  HTTP: Axios

大屏展示端:
  引擎: Unity 2022.3 LTS (URP)
  通信: NativeWebSocket 插件
  人脸识别: 虹软 ArcFace 4.x (离线SDK)
  视频播放: Unity VideoPlayer / AVPro

部署:
  Docker + Docker Compose
```

---

## 2. 系统架构

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  学生/校友端 H5  │  │  管理后台 Web    │  │  Unity 大屏端   │
│  - 资料填报      │  │  - 三级审核      │  │  - 3D轮播展示   │
│  - 进度查询      │  │  - 资料维护      │  │  - 人脸识别     │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │ HTTP/REST          │ HTTP/REST          │ WebSocket
         └────────────────────┴────────────────────┘
                              │
                  ┌───────────▼───────────┐
                  │  Spring Boot 后端     │
                  │  ┌─────────────────┐  │
                  │  │ 用户/认证模块    │  │
                  │  │ 资料管理模块    │  │
                  │  │ 审核流模块      │  │
                  │  │ 展示推送模块    │  │
                  │  │ 人脸库模块      │  │
                  │  └─────────────────┘  │
                  └───────────┬───────────┘
                              │
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
    ┌─────────┐         ┌──────────┐         ┌─────────┐
    │  MySQL  │         │  Redis   │         │  MinIO  │
    └─────────┘         └──────────┘         └─────────┘
```

---

## 3. 核心数据模型

```sql
-- 用户表（学生/校友/管理员）
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE,
  password VARCHAR(100),
  real_name VARCHAR(50),
  role TINYINT COMMENT '1学生 2校友 3院级审核 4教务处 5校级管理员',
  college_id BIGINT,
  major VARCHAR(50),
  enrollment_year INT,
  graduation_year INT,
  avatar_url VARCHAR(255),
  created_at DATETIME
);

-- 二级学院表
CREATE TABLE college (
  id BIGINT PRIMARY KEY,
  name VARCHAR(100),
  code VARCHAR(20)
);

-- 校友资料主表（核心）
CREATE TABLE alumni_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  title VARCHAR(200) COMMENT '资料标题',
  category TINYINT COMMENT '1荣誉 2作品 3成绩 4其他',
  description TEXT,
  cover_url VARCHAR(255),
  achieve_date DATE COMMENT '取得日期',
  status TINYINT COMMENT '0草稿 1院审中 2教务审中 3已发布 4驳回',
  reject_reason VARCHAR(500),
  is_on_shelf TINYINT DEFAULT 0 COMMENT '是否上架展示',
  display_weight INT DEFAULT 0 COMMENT '展示权重',
  view_count INT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

-- 资料附件表（图片/视频）
CREATE TABLE alumni_media (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  profile_id BIGINT,
  media_type TINYINT COMMENT '1图片 2视频 3文档',
  file_url VARCHAR(255),
  thumbnail_url VARCHAR(255),
  duration INT COMMENT '视频时长(秒)',
  sort_order INT
);

-- 审核记录表
CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  profile_id BIGINT,
  auditor_id BIGINT,
  audit_level TINYINT COMMENT '1院级 2校级',
  result TINYINT COMMENT '1通过 2驳回',
  comment VARCHAR(500),
  created_at DATETIME
);

-- 人脸特征表
CREATE TABLE face_feature (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT UNIQUE,
  feature_data BLOB COMMENT '人脸特征向量',
  feature_version VARCHAR(20),
  created_at DATETIME
);

-- 展示标签表
CREATE TABLE display_tag (
  id BIGINT PRIMARY KEY,
  name VARCHAR(50),
  color VARCHAR(20)
);

CREATE TABLE profile_tag (
  profile_id BIGINT,
  tag_id BIGINT,
  PRIMARY KEY(profile_id, tag_id)
);
```

---

## 4. 后端 API 设计

### 4.1 学生/校友端
```
POST   /api/profile/submit          提交资料（含附件URL）
GET    /api/profile/my              我的资料列表
GET    /api/profile/{id}            资料详情
PUT    /api/profile/{id}            修改（仅草稿/驳回状态）
DELETE /api/profile/{id}            删除
POST   /api/file/upload             文件上传(MinIO)
POST   /api/face/register           录入人脸
```

### 4.2 审核端
```
GET    /api/audit/pending           待审列表（按角色过滤）
POST   /api/audit/{id}/approve      通过
POST   /api/audit/{id}/reject       驳回
GET    /api/audit/history           审核历史
```

### 4.3 校级管理端
```
GET    /api/admin/profiles          全部已发布资料
PUT    /api/admin/profile/{id}/shelf      上架/下架
PUT    /api/admin/profile/{id}/weight     调整权重
POST   /api/admin/profile/{id}/tags       打标签
GET    /api/admin/dashboard               统计数据
```

### 4.4 大屏端（Unity 调用）
```
GET    /api/display/playlist        获取轮播列表（含资源URL）
GET    /api/display/profile/{id}    单条详情
POST   /api/display/face/recognize  上报识别到的人脸特征
                                    返回：匹配的userId+其专属内容
WebSocket: /ws/display              接收推送（管理员手动切换/通知刷新）
```

---

## 5. Unity 大屏端设计（重点）

### 5.1 场景结构
```
MainScene
├── BackgroundLayer        星空粒子 + 渐变背景
├── DisplayStage           主展示舞台
│   ├── CarouselMode       轮播模式（默认）
│   ├── ListMode           列表浏览模式
│   ├── SearchMode         查询模式
│   └── PersonalMode       个人专属模式（人脸识别触发）
├── UILayer                顶部Logo / 底部提示
├── FaceCameraController   摄像头管理
└── NetworkManager         API + WebSocket 通信
```

### 5.2 核心脚本架构
```
Scripts/
├── Core/
│   ├── GameManager.cs           全局状态机
│   ├── ConfigLoader.cs          配置加载
│   └── EventBus.cs              事件总线
├── Network/
│   ├── ApiClient.cs             REST API 封装
│   ├── WebSocketClient.cs       WS 长连接
│   └── DTOs/                    数据模型
├── Display/
│   ├── CarouselController.cs    轮播控制器
│   ├── ProfileCard.cs           单卡片组件
│   ├── MediaPlayer.cs           图/视频播放
│   ├── TransitionEffects.cs     转场特效
│   └── ParticleEffects.cs       粒子动效
├── FaceRecognition/
│   ├── ArcFaceWrapper.cs        虹软SDK封装
│   ├── FaceDetector.cs          实时检测
│   └── FaceMatchService.cs      匹配服务
└── UI/
    ├── ListView.cs
    ├── SearchPanel.cs
    └── PersonalView.cs
```

### 5.3 动态效果实现要点

**轮播模式核心动画**
```csharp
// 使用 DOTween 实现流畅动画
public class CarouselController : MonoBehaviour {
    public ProfileCard[] cards;
    public float interval = 8f;
    
    // 卡片切换：3D翻转 + 景深模糊
    void NextCard() {
        currentCard.transform.DORotate(new Vector3(0, 90, 0), 0.6f)
            .OnComplete(() => {
                LoadNextData();
                currentCard.transform.DORotate(Vector3.zero, 0.6f);
            });
        
        // 粒子爆发
        particleEffect.Play();
        
        // 后处理：临时增强Bloom
        DOTween.To(() => bloom.intensity.value, 
                   x => bloom.intensity.value = x, 
                   3f, 0.3f).SetLoops(2, LoopType.Yoyo);
    }
}
```

**关键动效清单（开发时实现）**
| 动效 | 实现方式 | 库 |
|-----|---------|-----|
| 卡片3D翻转 | Transform.DORotate | DOTween |
| 图片KenBurns | Scale+Position 缓动 | DOTween |
| 文字打字机 | TMPro + 字符动画 | TextMeshPro |
| 粒子转场 | ParticleSystem 爆发 | Unity内置 |
| Bloom辉光 | URP Volume 后处理 | URP |
| 时间轴展开 | LineRenderer + 节点弹出 | DOTween |
| 识别到人脸 | 镜头推近 + 光圈聚焦 | Cinemachine |

### 5.4 人脸识别流程
```csharp
// 每 500ms 检测一次
IEnumerator FaceLoop() {
    while(true) {
        var frame = webcam.GetCurrentFrame();
        var face = ArcFace.Detect(frame);
        
        if (face != null) {
            var feature = ArcFace.ExtractFeature(face);
            // 上报后端比对
            var result = await api.RecognizeFace(feature);
            
            if (result.matched && result.userId != currentUserId) {
                EventBus.Emit("PersonalModeStart", result.userId);
                // 触发个人专属轮播
            }
        }
        yield return new WaitForSeconds(0.5f);
    }
}
```

> ⚠️ 注：虹软 ArcFace 提供 C++ SDK，Unity 端需通过 C# DllImport 封装；建议把人脸特征提取放在 Unity 端，**只把特征向量上传后端比对**，避免传图。

---

## 6. 项目目录结构

```
exhibition-system/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/school/exhibition/
│   │   ├── ExhibitionApplication.java
│   │   ├── config/                   # 配置类
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── MinioConfig.java
│   │   │   └── SaTokenConfig.java
│   │   ├── modules/
│   │   │   ├── user/                 # 用户模块
│   │   │   ├── profile/              # 资料模块
│   │   │   ├── audit/                # 审核模块
│   │   │   ├── display/              # 展示模块
│   │   │   ├── face/                 # 人脸模块
│   │   │   └── file/                 # 文件模块
│   │   └── common/
│   │       ├── result/               # 统一响应
│   │       ├── exception/
│   │       └── utils/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── mapper/
│   └── pom.xml
│
├── admin-web/                        # 管理后台 Vue3
│   ├── src/
│   │   ├── api/
│   │   ├── views/
│   │   │   ├── login/
│   │   │   ├── audit/                # 审核工作台
│   │   │   ├── library/              # 资料库管理
│   │   │   ├── user/                 # 用户管理
│   │   │   └── dashboard/
│   │   ├── components/
│   │   ├── router/
│   │   └── stores/
│   └── package.json
│
├── student-h5/                       # 学生/校友端
│   ├── src/
│   │   ├── views/
│   │   │   ├── submit/               # 资料填报
│   │   │   ├── my-profile/           # 我的资料
│   │   │   └── face-register/        # 人脸录入
│   │   └── ...
│   └── package.json
│
├── unity-display/                    # Unity 大屏端
│   ├── Assets/
│   │   ├── Scenes/
│   │   ├── Scripts/                  # 详见 5.2
│   │   ├── Prefabs/
│   │   ├── Materials/
│   │   ├── Shaders/
│   │   ├── Plugins/
│   │   │   └── ArcFace/              # 虹软SDK
│   │   └── Resources/
│   └── ProjectSettings/
│
├── docker/
│   ├── docker-compose.yml            # MySQL+Redis+MinIO+Backend
│   └── nginx/
│
└── docs/
    ├── API.md
    ├── DEPLOY.md
    └── DEV-GUIDE.md
```

---

## 7. 关键业务流程

### 7.1 资料提交与审核流
```
学生提交资料 (status=1院审中)
       ↓
二级学院审核员看到待审列表
       ├─ 通过 → status=2 教务审中
       └─ 驳回 → status=4 驳回，学生可修改重提
       ↓
教务处审核
       ├─ 通过 → status=3 已发布，进入校友资料库
       └─ 驳回 → status=4
       ↓
校级管理员决定是否上架展示 (is_on_shelf=1)
       ↓
Unity大屏拉取 is_on_shelf=1 的数据进行展示
```

### 7.2 人脸识别个性化展示流
```
摄像头持续检测 (Unity端)
       ↓
检测到人脸 → ArcFace提取特征向量(1032字节)
       ↓
HTTP上传特征 → 后端遍历face_feature表比对
       ↓
返回 {matched: true, userId: 123, profiles: [...]}
       ↓
Unity切换到 PersonalMode
       ├─ 镜头推近特效
       ├─ 显示"欢迎回来，张三"
       └─ 加载该用户的资料专属轮播
       ↓
30秒无新人脸 → 回到默认轮播
```

---

## 8. 开发分阶段计划（建议给 Claude Code 的迭代节奏）

### **第一阶段：后端骨架（1-2周）**
- [ ] 初始化 Spring Boot 项目，集成 MyBatis Plus、Sa-Token
- [ ] 完成数据库建表脚本
- [ ] 实现用户登录、文件上传、资料 CRUD
- [ ] Docker Compose 一键启动 MySQL/Redis/MinIO

### **第二阶段：业务流程闭环（2周）**
- [ ] 三级审核流（角色权限+流转）
- [ ] 管理后台资料库管理（上架、权重、标签）
- [ ] 学生 H5 端资料提交+进度查询
- [ ] 大屏 API：playlist、profile 详情

### **第三阶段：Unity 大屏基础（2-3周）**
- [ ] Unity 项目搭建 + URP 配置
- [ ] API/WebSocket 通信封装
- [ ] 轮播模式（卡片+图片+视频播放）
- [ ] 列表浏览模式 + 查询模式
- [ ] 基础动效（DOTween 切换动画）

### **第四阶段：动态效果增强（2周）⭐核心**
- [ ] 粒子系统（背景+转场）
- [ ] URP 后处理（Bloom、景深、Vignette）
- [ ] KenBurns 图片缓推
- [ ] 时间轴成长轨迹动画
- [ ] 文字打字机、3D 翻转卡片

### **第五阶段：人脸识别集成（1-2周）**
- [ ] 虹软 SDK 在 Unity 中的 C# 封装
- [ ] H5 端人脸采集（调用摄像头拍照→上传）
- [ ] 后端特征比对接口（向量距离）
- [ ] Unity 端识别→个人专属模式切换

### **第六阶段：联调与优化（1周）**
- [ ] 4K 屏适配
- [ ] 性能优化（资源预加载、纹理压缩）
- [ ] 异常兜底（断网、识别失败）
- [ ] 部署文档

---

## 9. 给 Claude Code 的开发提示词模板

把下面的 Prompt 拆分使用，逐模块开发效果最佳：

### 启动项目
```
请基于以下技术栈初始化项目骨架：
- 后端：Spring Boot 3.2 + MyBatis Plus + Sa-Token + MySQL + Redis + MinIO
- 管理后台：Vue3 + TS + Vite + Element Plus + Pinia
- 学生H5：Vue3 + TS + Vite + Vant
- Unity 项目结构（仅创建目录与说明文件）

参考文档：[贴入本文档第 6 节目录结构]
要求：每个子项目可独立启动，提供 docker-compose.yml
```

### 开发后端模块
```
请实现【资料管理模块】：
- 数据库结构见：[第3节 alumni_profile / alumni_media / audit_log]
- API 见：[第4.1、4.2节]
- 业务流程见：[第7.1节]
要求：使用统一响应格式 R<T>，全局异常处理，参数校验。
```

### 开发 Unity 模块
```
请实现 Unity 轮播展示模块：
- 场景结构：[第5.1节]
- 核心脚本：CarouselController + ProfileCard + MediaPlayer
- 使用 DOTween 实现卡片3D翻转切换
- 使用 URP 后处理增强视觉
- 通过 ApiClient 拉取 /api/display/playlist 数据
- 每 8 秒切换，支持图片(KenBurns)和视频(自动播放)
代码风格：清晰注释，事件解耦
```

### 集成人脸识别
```
请集成虹软 ArcFace 到 Unity：
- SDK 路径：Assets/Plugins/ArcFace/
- 实现：FaceDetector（每500ms检测）+ ArcFaceWrapper（DllImport封装）
- 检测到人脸 → 提取特征 → POST /api/display/face/recognize
- 后端返回匹配的 userId → 切换到 PersonalMode
- 30秒无识别 → 回到默认轮播
参考：[第5.4节代码示例]
```

---

## 10. 风险与注意事项

| 风险点 | 应对方案 |
|------|---------|
| 虹软SDK授权 | 申请离线授权文件，注意机器绑定 |
| 4K视频卡顿 | 视频转码 H.265，使用 AVPro 插件 |
| 人脸隐私 | 学生授权同意书 + 特征加密存储 |
| Unity与Web通信 | WebSocket心跳保活，断线自动重连 |
| 大屏长时间运行 | 定时GC、内存监控、夜间自动重启 |
| 文件存储增长 | MinIO 配置生命周期，旧资源转冷存储 |

---

## ✅ 交付清单（项目完成时）

- [ ] 源码（backend / admin-web / student-h5 / unity-display）
- [ ] 数据库初始化 SQL
- [ ] Docker 部署脚本
- [ ] API 文档（Swagger）
- [ ] Unity 打包说明（Windows x64）
- [ ] 管理员/审核员/学生 操作手册
- [ ] 人脸录入流程说明

---
