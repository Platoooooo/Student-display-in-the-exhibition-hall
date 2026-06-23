# CLAUDE.md — 校友成果展览系统

> 高校校友优秀成果/荣誉/作品的全流程管理平台：校友填报 → 三级审核 → 大屏人脸识别个性化展示。
>
> 原始备份: `CLAUDE.md.bak`

## 工作约定

- **所有文件操作（创建、编辑、删除、新增）全部直接执行，不询问、不提示。**
- 遵循下方编码约定，优先复用现有模块模式。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2, MyBatis Plus, Sa-Token, JNA 5.14, Flowable 7 (optional) |
| 存储 | MySQL 8.0, Redis 7, MinIO |
| 管理后台 | Vue 3 + TS + Vite + Element Plus + Pinia + Axios |
| 学生 H5 | Vue 3 + TS + Vite + Vant |
| 大屏展示 | Vue 3 (CSS Animation: KenBurns, fade), getUserMedia (人脸扫描) |
| 人脸 | 虹软 ArcFace v3.0 C++ SDK → JNA 桥接（后端） / Mock 模式（开发） |
| 字体 | HarmonyOS Sans SC (Regular/Bold), Rajdhani SemiBold, DIN Bold |
| 通信 | REST + WebSocket (大屏推送) |
| 部署 | Docker Compose + PowerShell 编排脚本 |

## 系统架构

```
学生 H5 / 管理后台 / Vue 大屏
        ↓ HTTP REST / WebSocket
   Spring Boot (模块: 用户/资料/审核流/展示推送/人脸库)
        ↓                       ↓ JNA 调用
   MySQL + Redis + MinIO      ArcFace DLL (libarcsoft_face_engine)
```

## 核心数据模型（10 张表 + Flowable 自动建表）

- **college** — 二级学院(name/code)
- **sys_user** — 用户(5 角色: 学生/校友/院审/教务/校级管理)，归属学院
- **alumni_profile** — 资料主表(category 荣誉/作品/成绩/其他; status 草稿→院审中→教务审中→已发布→驳回; is_on_shelf 大屏上架; display_weight 权重)
- **alumni_media** — 图片/视频/文档附件
- **audit_log** — 审核记录(院级/校级, 通过/驳回)
- **face_feature** — 人脸特征向量(BLOB, 1032 字节), userId 1:1
- **display_tag / profile_tag** — 标签多对多
- **recognize_log** — 识别日志
- **sys_config** — 系统配置

## 核心 API 概览

- **学生端**: `POST /api/profile/draft|submit`, `GET/PUT/DELETE /api/profile/{id}`, `POST /api/file/upload`, `POST /api/face/register|extract`
- **审核端**: `GET /api/audit/pending`, `POST /api/audit/{id}/audit`, `GET /api/audit/{id}/history`
- **管理端**: `PUT /api/admin/profile/{id}/shelf|weight`, `POST .../tags`, `GET .../dashboard`
- **大屏端**: `GET /api/display/playlist`, `POST /api/face/recognize-image`, `WebSocket /ws/display`
- **人脸**: `POST /api/face/extract` (图片→特征), `POST /api/face/recognize-image` (图片→匹配)

## 关键业务流程

### 审核流（三级）
```
学生提交(status=1) → 院级审核(role=3) → 通过(status=2) → 教务处审核(role=4) → 通过(status=3 已发布)
                       ↓驳回(status=4)                     ↓驳回(status=4)
                 学生可修改重提                         学生可修改重提

已发布后: 校级管理(role=5) 设置 is_on_shelf=1 → Vue 大屏拉取展示
```

### 人脸识别流（Vue 版，当前）
```
浏览器 getUserMedia → 每 1.5s Canvas 截图 (320×240 JPEG)
→ POST /api/face/recognize-image (base64)
→ 后端 JNA 调用 ArcFace DLL 提取特征 (1032 字节)
→ 余弦比对 face_feature 表 → 返回 {matched, userId, profiles}
→ Vue 大屏切 PersonalMode → 15s 无新人脸回默认轮播
```
> ⚠️ 特征提取在**后端**（JNA 桥接），仅传 base64 图片到后端，图片不落盘。Mock 模式自动降级（无需 AppId/SDKKey）。

## 项目结构

```
exhibition-system/
├── backend/                    # Spring Boot (modules: user/profile/audit/display/face/file)
├── admin-web/                  # Vue3 管理后台 + 大屏展示
│   └── src/views/              # Dashboard/Audit/Library/Users/Tags/DisplayControl/DisplayScreen
├── student-h5/                 # Vue3 学生端 (submit/my-profile/face-register)
├── unity-display/              # Unity 大屏（暂停开发，由 Vue 替代）
├── docker/                     # docker-compose + nginx + SQL
├── art/fonts/                  # HarmonyOS Sans SC / Rajdhani / DIN
├── logs/                       # 运行日志
├── start-all.ps1               # 一键启动 Docker + Backend + Admin + H5
├── stop-all.ps1                # 一键停止
├── restart-all.ps1             # 一键重启
└── docs/
```

## Vue 大屏架构

- **模式切换**: CarouselMode(默认轮播) / PersonalMode(人脸识别) 二合一
- **Carousel**: 星空 CSS 背景 + KenBurns 缩放 + 标题/描述/标签卡 + 8s 自动切换 + 600ms fade 过渡
- **Personal**: 摄像头扫描 → 匹配成功 → 暂停轮播 → 展示用户专属列表 → 15s 无新人脸回默认
- **路由**: `/screen` 公开免登录
- **字体**: `@font-face` 声明 HarmonyOS Sans SC (Regular/Bold)

## 开发实际进展

| 阶段 | 内容 | 状态 |
|------|------|------|
| 1. 后端骨架 | Spring Boot + 建表 + 用户/文件/资料 CRUD + Docker | ✅ |
| 2. 业务闭环 | 三级审核流 + 管理后台 + H5 端 + 大屏 API | ✅ |
| 3. Unity 基础 | URP + API/WS + 轮播/列表/查询（暂停维护） | ⏸️ |
| 4. 动效增强 | 暂停（Vue CSS 动画替代） | ⏸️ |
| 5. 人脸识别 | ArcFace JNA 桥接 + H5 注册 + 后端比对 + Vue PersonalMode | ✅ |
| 6. Vue 大屏 | 全新 Vue 大屏 + 全栈编排脚本 + 字体/编码修复 | ✅ |

## 编码约定

- 后端统一响应格式 `R<T>`，全局异常处理，参数校验
- 前端 Pinia 状态管理，Axios 统一拦截（拦截器自动剥壳 `R.data`）
- 关键数据需日志，异常兜底（网络断开、识别失败等降级处理）
- 人脸特征加密存储，学生授权同意书
- CSS 动画优先（无运行时依赖），避免引入重型动效库

## 配置与环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| MYSQL_HOST | localhost | MySQL 地址 |
| REDIS_HOST | localhost | Redis 地址 |
| MINIO_ENDPOINT | http://localhost:9000 | MinIO API |
| ARCFACE_APP_ID | (空) | 虹软 ArcFace 激活码，为空则 Mock |
| ARCFACE_SDK_KEY | (空) | 虹软 SDK Key，为空则 Mock |
| FACE_THRESHOLD | 0.82 | 人脸匹配阈值 |

## 风险备忘

- 虹软 SDK v3.0 有效期至 2027-06-12，届时需续期或更换版本
- DLL 文件 (48MB+480KB) 需置于后端 `backend/target/` 目录（JNA `java.library.path`）
- Unity 项目已暂停但代码保留，恢复需 Unity 2022.3.62f3 + TMP 字体 SDF 烘焙
- WebSocket 心跳保活 (25s) + 5s 自动重连
- 生产环境务必修改默认密码、Redis 密码、MinIO 密钥
