# CLAUDE.md — 校友成果展览系统

> 高校校友优秀成果/荣誉/作品的全流程管理平台：校友填报 → 三级审核 → Unity 3D大屏人脸识别个性化展示。

## 工作约定

- **所有文件操作（创建、编辑、删除、新增）全部直接执行，不询问、不提示。** 本项目处于主动开发阶段，无需逐次确认。
- 遵循下方编码约定，优先复用现有模块模式。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2, MyBatis Plus, Sa-Token, Flowable 7 |
| 存储 | MySQL 8.0, Redis 7, MinIO |
| 管理后台 | Vue 3 + TS + Vite + Element Plus + Pinia + Axios |
| 学生H5 | Vue 3 + TS + Vite + Vant |
| 大屏 | Unity 2022.3 LTS (URP), DOTween, TextMeshPro, Cinemachine |
| 人脸 | 虹软 ArcFace 4.x (离线SDK, C++ → C# DllImport) |
| 通信 | REST + WebSocket (大屏推送) |
| 部署 | Docker Compose |

## 系统架构

```
学生H5 / 管理后台 Web / Unity大屏
        ↓ HTTP REST / WebSocket
   Spring Boot (模块: 用户/资料/审核流/展示推送/人脸库)
        ↓
   MySQL + Redis + MinIO
```

## 核心数据模型（8张表）

- **college** — 二级学院(name/code)
- **sys_user** — 用户(5角色: 学生/校友/院审/教务/校级管理)，归属学院
- **alumni_profile** — 资料主表(category 荣誉/作品/成绩/其他; status 草稿→院审中→教务审中→已发布→驳回; is_on_shelf 大屏上架; display_weight 权重)
- **alumni_media** — 图片/视频/文档附件
- **audit_log** — 审核记录(院级/校级, 通过/驳回)
- **face_feature** — 人脸特征向量(BLOB), userId 1:1
- **display_tag / profile_tag** — 标签多对多

## 核心 API 概览

- **学生端**: `POST /api/profile/submit`, `GET/PUT/DELETE /api/profile/{id}`, `POST /api/file/upload`, `POST /api/face/register`
- **审核端**: `GET /api/audit/pending`, `POST /api/audit/{id}/approve|reject`, `GET /api/audit/history`
- **管理端**: `PUT /api/admin/profile/{id}/shelf|weight`, `POST .../tags`, `GET .../dashboard`
- **大屏端**: `GET /api/display/playlist`, `GET /api/display/profile/{id}`, `POST /api/display/face/recognize`, `WebSocket /ws/display`

## 关键业务流程

### 审核流（三级）
```
学生提交(status=1) → 院级审核 → 通过(status=2) → 教务处审核 → 通过(status=3 已发布)
                       ↓驳回(status=4)                ↓驳回(status=4)
                 学生可修改重提                    学生可修改重提

已发布后: 校级管理设置 is_on_shelf=1 → Unity大屏拉取展示
```

### 人脸识别流
```
Unity摄像头每500ms检测人脸 → ArcFace提取特征向量(1032字节)
→ POST 后端遍历face_feature表比对 → 返回{matched, userId, profiles}
→ Unity切PersonalMode(镜头推近+欢迎动画+专属轮播) → 30s无新人脸回默认轮播
```
> ⚠️ 特征提取在Unity端，只传特征向量到后端，不传图片。

## 项目结构

```
exhibition-system/
├── backend/                    # Spring Boot (modules: user/profile/audit/display/face/file)
├── admin-web/                  # Vue3 管理后台 (audit/library/user/dashboard)
├── student-h5/                 # Vue3 学生端 (submit/my-profile/face-register)
├── unity-display/              # Unity大屏
│   └── Assets/Scripts/{Core,Network,Display,FaceRecognition,UI}/
├── docker/                     # docker-compose + nginx
└── docs/
```

## Unity 大屏架构

- **4种模式**: CarouselMode(默认轮播) / ListMode(列表) / SearchMode(查询) / PersonalMode(人脸触发)
- **核心脚本**: GameManager(状态机) → ApiClient + WebSocketClient → CarouselController + ProfileCard + MediaPlayer → ArcFaceWrapper → FaceDetector
- **动效**: DOTween(3D翻转/KenBurns/打字机) + URP后处理(Bloom/景深) + ParticleSystem + Cinemachine

## 开发分阶段（6阶段）

| 阶段 | 内容 | 周期 |
|------|------|------|
| 1. 后端骨架 | Spring Boot + 建表 + 用户/文件/资料CRUD + Docker | 1-2周 |
| 2. 业务闭环 | 三级审核流 + 管理后台 + H5端 + 大屏API | 2周 |
| 3. Unity基础 | URP项目 + API/WS通信 + 轮播/列表/查询模式 | 2-3周 |
| 4. 动效增强⭐ | 粒子/Bloom/KenBurns/时间轴/打字机/3D翻转 | 2周 |
| 5. 人脸识别 | ArcFace封装 + H5采集 + 后端比对 + Unity PersonalMode | 1-2周 |
| 6. 联调优化 | 4K适配 + 性能 + 异常兜底 + 部署文档 | 1周 |

## 编码约定

- 后端统一响应格式 `R<T>`，全局异常处理，参数校验
- Unity 模块事件解耦(EventBus)，清晰注释，Camera/Network 独立 GameObject
- 前端 Pinia 状态管理，Axios 统一拦截
- 关键数据需日志，异常兜底（网络断开、识别失败等降级处理）
- 人脸特征加密存储，学生授权同意书

## 风险备忘

- 虹软SDK需离线授权文件，注意机器绑定
- 4K视频用H.265编码，推荐AVPro插件
- WebSocket心跳保活+断线自动重连
- 大屏长时间运行需定时GC、内存监控、夜间自动重启
