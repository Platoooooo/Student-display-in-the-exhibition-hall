# 校友成果展览系统

> 高校校友优秀成果 / 荣誉 / 作品的全流程管理平台。
> 学生填报 → 三级审核 → 大屏人脸识别个性化展示。
>
> **开发模式**：Vibe Coding — 需求清晰、分段实现、快速迭代

## 项目背景

高校校友在学习、工作、生活中积累了丰富的优秀成果（荣誉奖项、作品、成绩等），这些成果散落在各个渠道，缺乏统一的管理和展示平台。本项目构建一套 **"校友填报 → 三级审核 → 大屏人脸识别个性化展示"** 的全流程管理系统，适用于校内展览馆、成果展示厅等场景。

### 核心亮点

- 🏆 **三级审核流**：院级审核 → 教务处审核 → 校级上架，保障成果质量
- 🖥️ **Vue 大屏展示**：全屏轮播、CSS 动画（KenBurns / 淡入淡出）、星空背景
- 👤 **人脸识别**：浏览器摄像头 → 后端 JNA 调用 ArcFace DLL → 特征比对 → 个性化展示
- 📱 **多端覆盖**：学生 H5 填报 + 管理后台审核 + Vue 大屏展示
- 🐳 **一键启停**：PowerShell 脚本编排（`start-all.ps1` / `stop-all.ps1` / `restart-all.ps1`）

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2 + MyBatis Plus + Sa-Token + JNA + MinIO + WebSocket |
| 存储 | MySQL 8 / Redis 7 / MinIO |
| 管理后台 | Vue 3 + TS + Vite + Element Plus + Pinia |
| 学生 H5 | Vue 3 + TS + Vite + Vant |
| 大屏展示 | Vue 3 + CSS Animation (KenBurns) + getUserMedia (人脸) |
| 人脸 | 虹软 ArcFace v3.0 离线 SDK（JNA 桥接，Mock/Real 自动切换） |
| 字体 | HarmonyOS Sans SC / Rajdhani / DIN |

## 项目结构

```
exhibition-system/
├── backend/              # Spring Boot 后端
│   └── src/main/java/com/school/exhibition/
│       ├── modules/{user,profile,audit,admin,display,face,file,tag,college}
│       ├── config/       # SaToken / WebSocket / MinIO / CORS / MybatisPlus
│       └── common/       # R、PageResult、BusinessException、GlobalExceptionHandler
├── admin-web/            # Vue3 管理后台 + 大屏展示
│   └── src/views/        # Dashboard / Audit / Library / Users / Tags / DisplayScreen
├── student-h5/           # Vue3 学生端 H5 (Vant)
│   └── src/views/        # Home / Submit / Me / ProfileDetail / FaceRegister
├── unity-display/        # Unity 大屏（暂时停用，由 Vue 替代）
│   └── Assets/Scripts/{Core,Network,Display,FaceRecognition,UI,DTOs}/
├── docker/               # docker-compose + nginx + SQL + 启动脚本
├── docs/                 # 需求文档 / 迭代记录 / 开发指南 / 视觉设计 / SQL
├── art/                  # 字体 / 贴图素材
├── logs/                 # 后端运行日志
├── start-all.ps1         # 一键启动全部服务
├── stop-all.ps1          # 一键停止全部服务
└── restart-all.ps1       # 一键重启全部服务
```

## 一键启动（开发环境）

依赖：JDK 17+、Maven 3.8+、Node.js 18+、Docker Desktop。

```powershell
# 一键启动全部服务（Docker + Backend + Admin + H5）
.\start-all.ps1

# 停止
.\stop-all.ps1

# 重启
.\restart-all.ps1
```

或手动分步启动：

```bash
cd docker
docker compose up -d mysql redis minio   # 基础设施
cd ../backend
mvn spring-boot:run                       # 后端 :8080
cd ../admin-web
npm install && npm run dev                # 管理后台 :5173
cd ../student-h5
npm install && npm run dev                # 学生 H5 :5174
```

### 启动后访问

| 页面 | 地址 | 说明 |
|------|------|------|
| 管理后台 | http://localhost:5173 | 审核 / 资料库 / 用户管理 |
| 学生 H5 | http://localhost:5174 | 成果提交 / 人脸录入 |
| 大屏展示 | http://localhost:5173/screen | 公开页面，免登录 |
| 后端 API | http://localhost:8080 | Swagger 未配置，见下方 API 表 |
| MinIO | http://localhost:9001 | minioadmin / minio123456 |

## 默认账号（密码均为 `123456`）

| 用户名 | 角色 | role | 说明 |
|---|---|---|---|
| admin | 校级管理员 | 5 | 上架、权重、用户/标签管理、大屏控制 |
| jiaowu | 教务处审核员 | 4 | 第二级审核 |
| cs_audit | 院级审核员（计信院） | 3 | 第一级审核 |
| student01 | 学生（张三） | 1 | 提交资料 / 录入人脸 |
| alumni01 | 校友（李四） | 2 | 提交资料 / 录入人脸 |

## 人脸识别流程（Vue 版）

```
浏览器摄像头 (getUserMedia) 每 1.5s 抓帧
→ 压缩为 JPEG base64 → POST /api/face/recognize-image
→ 后端 JNA 调用 ArcFace DLL 提取 1032 字节特征
→ 与 face_feature 表中所有注册特征余弦比对
→ 匹配成功 → 大屏切 PersonalMode → 15s 无新人脸回默认轮播
```

> ArcFace 未配置 AppId/SDKKey 时自动降级 Mock 模式（基于图像字节生成确定性特征，可联调）。
> 真机上线：设置环境变量 `ARCFACE_APP_ID` + `ARCFACE_SDK_KEY` → 重启后端。

## 三级审核状态机

```
0 草稿 ─submit─▶ 1 院审中 ─通过─▶ 2 教务审中 ─通过─▶ 3 已发布 ─上架─▶ 大屏展示
                  │ 驳回                │ 驳回
                  └────────▶ 4 驳回 ◀───┘
学生可在 0/4 状态修改重提；3 已发布需联系管理员先下架再修改
```

## 完整 API 表

| 模块 | 路径 | 说明 |
|---|---|---|
| 认证 | POST `/api/auth/login` · `/logout` · GET `/api/auth/me` | Sa-Token |
| 资料 | POST `/api/profile/draft` · `/submit` · GET/PUT/DELETE `/api/profile/{id}` · GET `/api/profile/my` | 学生 CRUD |
| 文件 | POST `/api/file/upload` (`?dir=cover\|media\|face\|avatar`) | MinIO |
| 审核 | GET `/api/audit/pending` · POST `/api/audit/{id}/audit` · GET `/api/audit/{id}/history` | 二级审核 |
| 用户 | GET `/api/user/list` · POST `/api/user/save` · PUT `/api/user/{id}/status\|reset-password` · `/change-password` | 校管 |
| 标签 | GET `/api/tag/list` · POST `/api/tag/save` · DELETE `/api/tag/{id}` | 标签 CRUD |
| 资料库 | GET `/api/admin/profile/library` · PUT `/api/admin/profile/{id}/shelf\|weight` · POST `/{id}/tags` | 上架 |
| 大屏 | GET `/api/display/playlist` · `/list` · `/search` · `/profile/{id}` | 轮播数据 |
| 人脸 | POST `/api/face/register` · `/extract` · `/recognize-image` · GET `/api/face/status` | 注册/识别 |

## Vue 大屏技术实现

- **CSS 星空背景**：N 层 `radial-gradient` 叠加模拟粒子
- **KenBurns 动画**：封面图 `scale(1.12)` 缓动 8s
- **卡片切换**：opacity + translateY 淡入淡出，600ms 过渡
- **人脸扫描**：`navigator.mediaDevices.getUserMedia` + Canvas 缩放到 320x240 → 每 1.5s POST 识别
- **PersonalMode**：匹配后暂停轮播，展示该用户的专属成果列表，15s 无新人脸退出

## 部署

```bash
cd backend && mvn clean package -DskipTests
cd ../admin-web  && npm run build
cd ../student-h5 && npm run build
# 拷贝 dist 至 docker/nginx/html/
cd ../docker && docker compose up -d
```

## 迭代记录

详见 [docs/迭代记录.md](docs/迭代记录.md)。5 个阶段，20 次迭代。

| 阶段 | 内容 |
|------|------|
| 1. 后端骨架 (2026-06-12) | Spring Boot + 建表 + 用户/文件/资料 CRUD + Docker |
| 2. 业务闭环 (2026-06-13) | 三级审核流 + 管理后台 + H5 端 + 大屏 API |
| 3. Unity 动效 (2026-06-14) | URP + DOTween + 轮播/列表/查询 + 人脸识别 |
| 4. 安全加固 (2026-06-15~16) | 安全头/限流/端口绑定 + Flowable 修复 + TS 兼容 |
| 5. Vue 大屏 (2026-06-23) | Vue 替代 Unity 大屏 + JNA 人脸桥接 + 一键启停 |
