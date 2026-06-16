# 校友成果展览系统

> 高校校友优秀成果 / 荣誉 / 作品的全流程管理平台。
> 学生填报 → 三级审核 → 大屏（Unity）人脸识别个性化展示。
>
> **开发模式**：Vibe Coding — 需求清晰、分段实现、快速迭代

## 项目背景

高校校友在学习、工作、生活中积累了丰富的优秀成果（荣誉奖项、作品、成绩等），这些成果散落在各个渠道，缺乏统一的管理和展示平台。本项目构建一套 **"校友填报 → 三级审核 → 大屏人脸识别个性化展示"** 的全流程管理系统，适用于校内展览馆、成果展示厅等场景。

### 核心亮点

- 🏆 **三级审核流**：院级审核 → 教务处审核 → 校级上架，保障成果质量
- 🖥️ **Unity 3D 大屏**：URP 渲染管线，4 种展示模式，KenBurns + Bloom + 粒子特效
- 👤 **人脸识别**：ArcFace 离线 SDK，摄像头抓帧 → 特征提取 → 后端比对 → 个性化展示
- 📱 **多端覆盖**：学生 H5 填报 + 管理后台审核 + Unity 大屏展示
- 🐳 **一键部署**：Docker Compose 编排，开发/生产环境统一

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2 + MyBatis Plus + Sa-Token + MinIO + WebSocket |
| 存储 | MySQL 8 / Redis 7 / MinIO |
| 管理后台 | Vue 3 + TS + Vite + Element Plus + Pinia |
| 学生 H5 | Vue 3 + TS + Vite + Vant |
| 大屏 | Unity 2022.3 LTS (URP) + DOTween + Cinemachine + Newtonsoft.Json + NativeWebSocket |
| 人脸 | 虹软 ArcFace 4.x（C# DllImport，Mock 模式可无 SDK 联调） |

## 项目结构

```
exhibition-system/
├── backend/        # Spring Boot 后端
│   └── src/main/java/com/school/exhibition/
│       ├── modules/{user,profile,audit,admin,display,face,file,tag,college}
│       ├── config/    # SaToken / WebSocket / Minio / CORS / MybatisPlus
│       └── common/    # R、PageResult、BusinessException、GlobalExceptionHandler
├── admin-web/      # Vue3 管理后台
│   └── src/views/  # Dashboard、Audit、Library、Users、Tags、DisplayControl
├── student-h5/     # Vue3 学生端 H5 (Vant)
│   └── src/views/  # Home、Submit（创建/编辑）、Me、ProfileDetail、FaceRegister
├── unity-display/  # Unity 大屏
│   └── Assets/Scripts/{Core,Network,Display,FaceRecognition,UI,DTOs}/
├── docker/         # docker-compose + nginx + 初始化 SQL + 启动脚本
├── docs/           # 需求文档 / 迭代记录 / 开发指南 / 视觉设计 / SQL
└── screenshots/    # 系统全流程运行截图
```

## 一键启动（开发环境）

依赖：JDK 17+、Maven 3.8+、Node.js 18+、Docker Desktop。

```bash
# 1) 起基础组件（MySQL/Redis/MinIO）
cd docker
docker compose up -d mysql redis minio

# 2) 启动后端
cd ../backend
mvn spring-boot:run

# 3) 启动管理后台
cd ../admin-web
npm install && npm run dev   # http://localhost:5173

# 4) 启动学生端
cd ../student-h5
npm install && npm run dev   # http://localhost:5174

# 5) Unity：用 Unity Hub 打开 unity-display 工程，运行 MainScene
```

或直接执行 [docker/start.sh](docker/start.sh) / [docker/start.ps1](docker/start.ps1)。

## 默认账号（密码均为 `123456`）

| 用户名 | 角色 | 说明 |
|---|---|---|
| admin | 校级管理员 | 上架、权重、用户/标签管理、Dashboard、大屏控制 |
| jiaowu | 教务处 | 第二级审核 |
| cs_audit | 院级审核（计信院） | 第一级审核 |
| student01 | 学生 | 提交资料 / 录入人脸 |
| alumni01 | 校友 | 提交资料 / 录入人脸 |

## 核心 API（全部以 `/api` 开头）

| 模块 | 路径 | 说明 |
|---|---|---|
| 认证 | POST `/auth/login` · `/auth/logout` · GET `/auth/me` | Sa-Token，header `Authorization: <token>` |
| 学院 | GET `/college/list` | 学院列表（开放） |
| 资料 | POST `/profile/draft` · `/submit` · GET `/profile/{id}` · DELETE `/profile/{id}` · GET `/profile/my` | 学生端 CRUD（驳回可编辑重提） |
| 文件 | POST `/file/upload` (`?dir=cover\|media\|face\|avatar`) | MinIO 上传 |
| 审核 | GET `/audit/pending` · POST `/audit/{id}/audit` · GET `/audit/{id}/history` | 三级审核 |
| 用户管理 | GET `/user/list` · POST `/user/save` · PUT `/user/{id}/status` · `/{id}/reset-password` · `/change-password` · `/profile` | 校管/教务可用 |
| 标签 | GET `/tag/list` · POST `/tag/save` · DELETE `/tag/{id}` | 标签 CRUD |
| 后台资料库 | GET `/admin/profile/library` · PUT `/admin/profile/{id}/shelf\|weight` · POST `/admin/profile/{id}/tags` | 上架/权重/标签 |
| Dashboard | GET `/admin/dashboard` | 概览 + 学院/类目分布 + 近 7 天识别趋势 |
| 大屏控制 | POST `/admin/display/push/profile/{id}` · `/push/notice` · `/refresh` · GET `/admin/display/online` | 通过 WS 控制大屏 |
| 大屏端 | GET `/display/playlist` · `/list` · `/search?keyword=` · `/profile/{id}` · POST `/display/face/recognize` · WS `/ws/display` | Unity 接入 |
| 人脸 | POST `/face/register` · GET `/face/status` | 学生录入 |

## 三级审核状态机

```
0 草稿 ─submit─▶ 1 院审中 ─通过─▶ 2 教务审中 ─通过─▶ 3 已发布 ─上架─▶ 大屏展示
                  │ 驳回                │ 驳回
                  └────────▶ 4 驳回 ◀───┘
学生可在 0/4 状态修改重提；3 已发布需联系管理员先下架再修改
```

## Unity 大屏 4 种模式

| 模式 | 触发 | 行为 |
|---|---|---|
| Carousel（默认轮播） | 启动 / Personal 退出 / `REFRESH_PLAYLIST` | 双卡片交叉切换，KenBurns + Bloom + 粒子转场 |
| List（列表浏览） | 顶部按钮 / `SWITCH_MODE` | 网格瀑布流 + 类目筛选 + 滚动加载 |
| Search（关键词搜索） | 顶部按钮 | 输入关键字 → `/api/display/search` |
| Personal（人脸专属） | `FaceDetector` 识别命中 | 镜头推近 + 欢迎语 + 个人作品轮播，30s 无新人脸自动退出 |

WebSocket 消息类型：`REFRESH_PLAYLIST` / `FORCE_PROFILE` / `SWITCH_MODE` / `NOTICE` / `WELCOME` / `PONG`，
Unity 端 25s 心跳 + 5s 自动重连。

## 人脸识别流程

```
摄像头每 500ms 抓帧 → ArcFaceWrapper.DetectAndExtract（1032 字节特征）
→ Base64 上报 /api/display/face/recognize → 后端遍历 face_feature 表余弦比对
→ matched=true → Unity 切 PersonalMode
```

> ArcFaceWrapper 默认 `useMock=true`：用图像哈希生成确定性特征，无虹软 SDK 也能联调。
> 真机上线：`ArcFaceWrapper.Init(appId, sdkKey, useMock: false)` 并部署虹软 dll。

## 迭代记录

本项目采用 **Vibe Coding** 开发模式，分 4 个阶段、16 次迭代完成。详细迭代记录见 [docs/迭代记录.md](docs/迭代记录.md)。

| 阶段 | 内容 | 关键迭代 |
|------|------|----------|
| 1. 后端骨架 | Spring Boot + 建表 + 用户/文件/资料CRUD + Docker | 3 次 commit |
| 2. 业务闭环 | 三级审核流 + 管理后台 + H5端 + 大屏API | 4 次 commit |
| 3. Unity基础与动效 | URP项目 + API/WS通信 + 轮播/列表/查询模式 + 人脸识别 | 3 次 commit |
| 4. 安全加固与部署 | 安全头/速率限制/端口绑定 + 性能优化 + 兼容性修复 | 6 次 commit |

> **最新迭代** (2026-06-16)：package-lock.json 锁定依赖版本、TS 5.0 兼容性修复、TS 类型错误修复、Map 导入补充。

## 项目截图

系统运行截图存放于 [screenshots/](screenshots/) 目录，覆盖学生端 H5、管理后台、Unity 大屏、部署运维四大模块。

## 部署

```bash
cd backend && mvn clean package -DskipTests
cd ../admin-web  && npm run build
cd ../student-h5 && npm run build
# 拷贝 admin-web/dist 至 docker/nginx/html/admin
# 拷贝 student-h5/dist 至 docker/nginx/html/student
cd ../docker && docker compose up -d
```

访问：

- 管理后台：http://localhost (host 加 `127.0.0.1 admin.exhibition.local`)
- 学生 H5：http://localhost (host 加 `127.0.0.1 student.exhibition.local`)
- MinIO 控制台：http://localhost:9001 (minioadmin / minio123456)

## 开发要点

- **统一响应**：所有接口返回 `R<T> { code, msg, data }`，前端拦截器自动剥壳。
- **角色守卫**：管理后台路由按 `meta.roles` 自动过滤侧边菜单（教务/校管才能看到用户/标签/大屏控制）。
- **人脸识别**：特征向量在 Unity 端用 ArcFace 提取，仅上传 1032 字节 base64 到后端，**不传图**。
- **大屏推送**：管理后台上架/调权/推送 → 后端 WebSocket 广播 → Unity 自动响应。
- **MinIO**：bucket 默认设置匿名读策略，方便 Unity 直接走 URL 访问；生产环境应改预签名。

## 风险备忘

- 虹软 SDK 需离线授权（机器绑定），Demo 当前默认 Mock 模式。
- 4K 视频推荐 H.265 + AVPro 插件。
- WebSocket 心跳保活（25s）+ 5s 自动重连。
- 大屏建议夜间定时重启（避免长时间运行内存泄漏）。
- 生产环境务必修改默认密码、Redis 密码、MinIO 密钥。
